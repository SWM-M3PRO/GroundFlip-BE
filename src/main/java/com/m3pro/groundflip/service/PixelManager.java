package com.m3pro.groundflip.service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
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
import com.m3pro.groundflip.repository.PixelUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PixelManager {
	private static final String REDISSON_LOCK_PREFIX = "LOCK:";
	private static final Long DEFAULT_COMMUNITY_ID = -1L;
	private static final int WGS84_SRID = 4326;
	private static final double lat_per_pixel = 0.000724;
	private static final double lon_per_pixel = 0.000909;
	private static final double upper_left_lat = 38.240675;
	private static final double upper_left_lon = 125.905952;

	private final PixelRepository pixelRepository;
	private final UserRankingService userRankingService;
	private final CommunityRankingService communityRankingService;
	private final ApplicationEventPublisher eventPublisher;
	private final RedissonClient redissonClient;
	private final PixelUserRepository pixelUserRepository;
	private final GeometryFactory geometryFactory;

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
		Long occupyingCommunityId = Optional.ofNullable(pixelOccupyRequest.getCommunityId()).orElse(-1L);

		Pixel targetPixel = pixelRepository.findByXAndY(pixelOccupyRequest.getX(), pixelOccupyRequest.getY())
			.orElseThrow(() -> new AppException(ErrorCode.PIXEL_NOT_FOUND));

		userRankingService.updateCurrentPixelRanking(targetPixel, occupyingUserId);
		updateUserAccumulatePixelCount(targetPixel, occupyingUserId);
		updatePixelOwnerUser(targetPixel, occupyingUserId);

		updateCommunityCurrentPixelCount(targetPixel, occupyingCommunityId);
		updateCommunityAccumulatePixelCount(targetPixel, occupyingCommunityId);
		updatePixelOwnerCommunity(targetPixel, occupyingCommunityId);

		pixelRepository.saveAndFlush(targetPixel);

		updatePixelAddress(targetPixel);
		eventPublisher.publishEvent(
			new PixelUserInsertEvent(targetPixel.getId(), occupyingUserId, occupyingCommunityId));
	}

	private Pixel createPixel(Long x, Long y) {
		Long pixelId = getPixelId(x, y);
		Point coordinate = getCoordinate(x, y);
		
		return Pixel.builder()
			.id(pixelId)
			.x(x)
			.y(y)
			.coordinate(coordinate)
			.build();

	}

	private Point getCoordinate(Long x, Long y) {
		double currentLongitude = upper_left_lon + (y * lon_per_pixel);
		double currentLatitude = upper_left_lat - (x * lat_per_pixel);
		Point point = geometryFactory.createPoint(new Coordinate(currentLongitude, currentLatitude));
		point.setSRID(WGS84_SRID);
		return point;
	}

	private Long getPixelId(Long x, Long y) {
		return x * 4156 + y + 1;
	}

	private void updateCommunityAccumulatePixelCount(Pixel targetPixel, Long communityId) {
		if (!pixelUserRepository.existsByPixelIdAndCommunityId(targetPixel.getId(), communityId)) {
			if (!communityId.equals(DEFAULT_COMMUNITY_ID)) {
				communityRankingService.updateAccumulatedRanking(communityId);
			}
		}
	}

	private void updateUserAccumulatePixelCount(Pixel targetPixel, Long userId) {
		if (!pixelUserRepository.existsByPixelIdAndUserId(targetPixel.getId(), userId)) {
			userRankingService.updateAccumulatedRanking(userId);
		}
	}

	private void updateCommunityCurrentPixelCount(Pixel targetPixel, Long communityId) {
		if (!communityId.equals(DEFAULT_COMMUNITY_ID)) {
			communityRankingService.updateCurrentPixelRanking(targetPixel, communityId);
		}
	}

	private void updatePixelOwnerUser(Pixel targetPixel, Long occupyingUserId) {
		targetPixel.updateUserId(occupyingUserId);
		targetPixel.updateUserOccupiedAtToNow();
	}

	private void updatePixelOwnerCommunity(Pixel targetPixel, Long occupyingCommunityId) {
		if (!occupyingCommunityId.equals(DEFAULT_COMMUNITY_ID)) {
			targetPixel.updateCommunityId(occupyingCommunityId);
			targetPixel.updateCommunityOccupiedAtToNow();
		}
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
