package com.m3pro.groundflip.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m3pro.groundflip.domain.dto.pixel.IndividualHistoryPixelResponse;
import com.m3pro.groundflip.domain.dto.pixel.IndividualModePixelResponse;
import com.m3pro.groundflip.domain.dto.pixel.IndividualPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixel.PixelCountResponse;
import com.m3pro.groundflip.domain.dto.pixel.PixelOccupyRequest;
import com.m3pro.groundflip.domain.dto.pixel.PixelOwnerUserResponse;
import com.m3pro.groundflip.domain.dto.pixel.VisitedUserInfo;
import com.m3pro.groundflip.domain.dto.pixel.event.PixelAddressUpdateEvent;
import com.m3pro.groundflip.domain.dto.pixel.event.PixelUserInsertEvent;
import com.m3pro.groundflip.domain.dto.pixelUser.IndividualHistoryPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixelUser.VisitedUser;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.domain.entity.global.BaseTimeEntity;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.PixelRepository;
import com.m3pro.groundflip.repository.PixelUserRepository;
import com.m3pro.groundflip.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PixelService {
	private static final int WGS84_SRID = 4326;
	private final GeometryFactory geometryFactory;
	private final PixelRepository pixelRepository;
	private final PixelUserRepository pixelUserRepository;
	private final UserRepository userRepository;
	private final RankingService rankingService;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * 사용자를 중심으로 일정한 반경 내에 개인전 픽셀들을 가져온다.
	 * @param currentLatitude 사용자의 위도
	 * @param currentLongitude 사용자의 경도
	 * @param radius 반경
	 * @return List<IndividualModePixelResponse> 픽셀의 정보가 담긴 리스트
	 * @author 김민욱
	 */
	public List<IndividualModePixelResponse> getNearIndividualModePixelsByCoordinate(
		double currentLatitude,
		double currentLongitude,
		int radius
	) {
		Point point = geometryFactory.createPoint(new Coordinate(currentLongitude, currentLatitude));
		point.setSRID(WGS84_SRID);

		return pixelRepository.findAllIndividualModePixelsByCoordinate(point, radius);
	}

	/**
	 * 위도 경도를 기준으로 radius 범위 안에서 userId 의 유저가 방문한 픽셀을 가져온다,.
	 * @param currentLatitude 사용자의 위도
	 * @param currentLongitude 사용자의 경도
	 * @param radius 반경
	 * @param userId 사용자의 id
	 * @return 픽셀의 정보가 담긴 리스트
	 */
	public List<IndividualHistoryPixelResponse> getNearIndividualHistoryPixelsByCoordinate(double currentLatitude,
		double currentLongitude, int radius, Long userId) {
		Point point = geometryFactory.createPoint(new Coordinate(currentLongitude, currentLatitude));
		point.setSRID(WGS84_SRID);

		return pixelRepository.findAllIndividualPixelsHistoryByCoordinate(point, radius, userId);
	}

	/**
	 * 특정 픽셀의 정보를 반환한다. 소유주의 정보, 오늘 방문한 사람의 수, 방문한 사람 리스트
	 * @param pixelId 찾고자 하는 픽셀의 id
	 * @return 소유주의 정보, 오늘 방문한 사람의 수, 방문한 사람 리스트
	 */
	public IndividualPixelInfoResponse getIndividualModePixelInfo(Long pixelId) {
		Pixel pixel = pixelRepository.findById(pixelId)
			.orElseThrow(() -> new AppException(ErrorCode.PIXEL_NOT_FOUND));

		List<VisitedUser> visitedUsers = pixelUserRepository.findAllVisitedUserByPixelId(pixelId);
		PixelOwnerUserResponse pixelOwnerUserResponse = getPixelOwnerUserInfo(pixel);

		return IndividualPixelInfoResponse.from(
			pixel,
			pixelOwnerUserResponse,
			visitedUsers.stream().map(VisitedUserInfo::from).toList()
		);
	}

	/**
	 * 픽셀을 차지한다.
	 * @param pixelOccupyRequest 픽셀을 차지하기 위해 필요한 정보
	 * @return
	 * @author 김민욱
	 */
	@Transactional
	public void occupyPixel(PixelOccupyRequest pixelOccupyRequest) {
		Long occupyingUserId = pixelOccupyRequest.getUserId();
		Long communityId = Optional.ofNullable(pixelOccupyRequest.getCommunityId()).orElse(-1L);

		Pixel targetPixel = pixelRepository.findByXAndY(pixelOccupyRequest.getX(), pixelOccupyRequest.getY())
			.orElseThrow(() -> new AppException(ErrorCode.PIXEL_NOT_FOUND));

		updateRankingOnCache(targetPixel, occupyingUserId);
		targetPixel.updateUserId(occupyingUserId);

		updatePixelAddress(targetPixel);
		eventPublisher.publishEvent(new PixelUserInsertEvent(targetPixel.getId(), occupyingUserId, communityId));
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

	/**
	 * 레디스 상에서 랭킹을 조정한다.
	 * @param targetPixel 랭킹을 조정할 픽셀
	 * @param occupyingUserId 현재 픽셀을 방문한 유저
	 * @return
	 * @author 김민욱
	 */
	private void updateRankingOnCache(Pixel targetPixel, Long occupyingUserId) {
		Long originalOwnerUserId = targetPixel.getUserId();
		if (Objects.equals(originalOwnerUserId, occupyingUserId)) {
			return;
		}

		if (originalOwnerUserId == null) {
			rankingService.increaseCurrentPixelCount(occupyingUserId);
		} else {
			rankingService.updateRankingAfterOccupy(occupyingUserId, originalOwnerUserId);
		}
	}

	/**
	 * 개인 기록 모드에서 픽셀 방문 기록을 가져온다
	 * @param pixelId 기록을 조회할 픽셀
	 * @param userId 기록을 조회할 사용자
	 * @return IndividualHistoryPixelInfoResponse 기록, 방문 횟수 등을 담고 있는 객체
	 * @author 김민욱
	 */
	public IndividualHistoryPixelInfoResponse getIndividualHistoryPixelInfo(Long pixelId, Long userId) {
		Pixel pixel = pixelRepository.findById(pixelId)
			.orElseThrow(() -> new AppException(ErrorCode.PIXEL_NOT_FOUND));

		User user = userRepository.getReferenceById(userId);

		List<LocalDateTime> visitList = pixelUserRepository.findAllVisitHistoryByPixelAndUser(pixel, user).stream()
			.map(BaseTimeEntity::getCreatedAt)
			.toList();

		return IndividualHistoryPixelInfoResponse.from(pixel, visitList);
	}

	/**
	 * 현재 소유하고 있는 픽셀, 누적으로 방문한 픽셀 갯수를 구한다.
	 * @param userId 픽셀 개수를 조회할 사용자
	 * @return PixelCountResponse 현재 픽셀, 누적 픽셀을 담고있는 객체
	 * @author 김민욱
	 */
	public PixelCountResponse getPixelCount(Long userId) {
		return PixelCountResponse.builder()
			.currentPixelCount(rankingService.getCurrentPixelCountFromCache(userId))
			.accumulatePixelCount(pixelUserRepository.countAccumulatePixelByUserId(userId))
			.build();
	}

	/**
	 * 픽셀의 소유주의 정보를 반환한다.
	 * @param pixel 소유주의 정보를 알고 싶은 픽셀
	 * @return 닉네임, 프로필 사진, 현재 픽실, 누적 픽셀
	 */
	private PixelOwnerUserResponse getPixelOwnerUserInfo(Pixel pixel) {
		Long ownerUserId = pixel.getUserId();
		if (ownerUserId == null) {
			return null;
		} else {
			Long accumulatePixelCount = pixelUserRepository.countAccumulatePixelByUserId(ownerUserId);
			Long currentPixelCount = rankingService.getCurrentPixelCountFromCache(ownerUserId);
			User ownerUser = userRepository.findById(ownerUserId)
				.orElseThrow(() -> {
					log.error("pixel {} 의 소유자가 {} 인데 존재하지 않음.", pixel.getId(), ownerUserId);
					return new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
				});
			return PixelOwnerUserResponse.from(ownerUser, currentPixelCount, accumulatePixelCount);
		}
	}
}
