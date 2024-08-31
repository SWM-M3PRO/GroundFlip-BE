package com.m3pro.groundflip.service;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m3pro.groundflip.domain.dto.pixel.PixelOccupyRequest;
import com.m3pro.groundflip.domain.dto.pixel.event.PixelAddressUpdateEvent;
import com.m3pro.groundflip.domain.dto.pixel.event.PixelUserInsertEvent;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.PixelRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PixelManager {
	private static final String REDISSON_LOCK_PREFIX = "LOCK:";

	private final PixelRepository pixelRepository;
	private final RankingService rankingService;
	private final ApplicationEventPublisher eventPublisher;
	private final RedissonClient redissonClient;

	/**
	 * 픽셀을 차지한다.
	 * @param pixelOccupyRequest 픽셀을 차지하기 위해 필요한 정보
	 * @return
	 * @author 김민욱
	 */
	@Transactional
	public void occupyPixelWithLock(PixelOccupyRequest pixelOccupyRequest) {
		String lockName = REDISSON_LOCK_PREFIX + pixelOccupyRequest.getX() + pixelOccupyRequest.getY();
		RLock rLock = redissonClient.getLock(lockName);

		long waitTime = 5L;
		long leaseTime = 3L;
		TimeUnit timeUnit = TimeUnit.SECONDS;
		try {
			boolean available = rLock.tryLock(waitTime, leaseTime, timeUnit);
			if (!available) {
				throw new AppException(ErrorCode.LOCK_ACQUISITION_ERROR);
			}

			occupyPixel(pixelOccupyRequest);

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			rLock.unlock();
		}
	}

	private void occupyPixel(PixelOccupyRequest pixelOccupyRequest) {
		Long occupyingUserId = pixelOccupyRequest.getUserId();
		Long communityId = Optional.ofNullable(pixelOccupyRequest.getCommunityId()).orElse(-1L);

		Pixel targetPixel = pixelRepository.findByXAndY(pixelOccupyRequest.getX(), pixelOccupyRequest.getY())
			.orElseThrow(() -> new AppException(ErrorCode.PIXEL_NOT_FOUND));
		rankingService.updateRanking(targetPixel, occupyingUserId);
		updatePixelOwner(targetPixel, occupyingUserId);

		updatePixelAddress(targetPixel);
		eventPublisher.publishEvent(new PixelUserInsertEvent(targetPixel.getId(), occupyingUserId, communityId));
	}

	private void updatePixelOwner(Pixel targetPixel, Long occupyingUserId) {
		if (Objects.equals(targetPixel.getUserId(), occupyingUserId)) {
			targetPixel.updateModifiedAtToNow();
		} else {
			targetPixel.updateUserId(occupyingUserId);
		}
		pixelRepository.saveAndFlush(targetPixel);
	}

	/**
	 * 픽셀의 주소를 업데이트한다..
	 * @param targetPixel 주소를 얻기 위한 픽셀
	 * @return
	 * @author 김민욱
	 */
	private void updatePixelAddress(Pixel targetPixel) {
		if (targetPixel.getAddress() == null) {
			eventPublisher.publishEvent(new PixelAddressUpdateEvent(targetPixel));
		}
	}
}
