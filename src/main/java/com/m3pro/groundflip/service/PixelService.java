package com.m3pro.groundflip.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m3pro.groundflip.domain.dto.pixel.IndividualHistoryPixelResponse;
import com.m3pro.groundflip.domain.dto.pixel.IndividualModePixelResponse;
import com.m3pro.groundflip.domain.dto.pixel.IndividualPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixel.PixelCountResponse;
import com.m3pro.groundflip.domain.dto.pixel.PixelOccupyRequest;
import com.m3pro.groundflip.domain.dto.pixel.PixelOwnerUserResponse;
import com.m3pro.groundflip.domain.dto.pixel.VisitedUserInfo;
import com.m3pro.groundflip.domain.dto.pixelUser.IndividualHistoryPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixelUser.VisitedUser;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.domain.entity.PixelUser;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.domain.entity.global.BaseTimeEntity;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.CommunityRepository;
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
	private final CommunityRepository communityRepository;
	private final UserRepository userRepository;
	private final ReverseGeoCodingService reverseGeoCodingService;
	private final RankingService rankingService;

	public List<IndividualModePixelResponse> getNearIndividualModePixelsByCoordinate(
		double currentLatitude,
		double currentLongitude,
		int radius
	) {
		Point point = geometryFactory.createPoint(new Coordinate(currentLongitude, currentLatitude));
		point.setSRID(WGS84_SRID);

		return pixelRepository.findAllIndividualModePixelsByCoordinate(point, radius);
	}

	public List<IndividualHistoryPixelResponse> getNearIndividualHistoryPixelsByCoordinate(double currentLatitude,
		double currentLongitude, int radius, Long userId) {
		Point point = geometryFactory.createPoint(new Coordinate(currentLongitude, currentLatitude));
		point.setSRID(WGS84_SRID);

		return pixelRepository.findAllIndividualPixelsHistoryByCoordinate(point, radius, userId);
	}

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

	@Transactional
	public void occupyPixel(PixelOccupyRequest pixelOccupyRequest) {
		Long communityId = Optional.ofNullable(pixelOccupyRequest.getCommunityId()).orElse(-1L);
		Long occupyingUserId = pixelOccupyRequest.getUserId();

		Pixel targetPixel = pixelRepository.findByXAndY(pixelOccupyRequest.getX(), pixelOccupyRequest.getY())
			.orElseThrow(() -> new AppException(ErrorCode.PIXEL_NOT_FOUND));
		updatePixelAddress(targetPixel);
		updatePixelOwnerUser(targetPixel, occupyingUserId);

		PixelUser pixelUser = PixelUser.builder()
			.pixel(targetPixel)
			.community(communityRepository.getReferenceById(communityId))
			.user(userRepository.getReferenceById(occupyingUserId))
			.build();
		pixelUserRepository.save(pixelUser);
	}

	private void updatePixelAddress(Pixel targetPixel) {
		if (targetPixel.getAddress() == null) {
			String address = reverseGeoCodingService.getAddressFromCoordinates(targetPixel.getCoordinate().getX(),
				targetPixel.getCoordinate().getY());
			targetPixel.updateAddress(address);
		}
	}

	private void updatePixelOwnerUser(Pixel targetPixel, Long occupyingUserId) {
		Long originalOwnerUserId = targetPixel.getUserId();
		if (Objects.equals(originalOwnerUserId, occupyingUserId)) {
			return;
		}
		targetPixel.updateUserId(occupyingUserId);

		if (originalOwnerUserId == null) {
			rankingService.increaseCurrentPixelCount(occupyingUserId);
		} else {
			rankingService.updateRankingAfterOccupy(occupyingUserId, originalOwnerUserId);
		}
	}

	public IndividualHistoryPixelInfoResponse getIndividualHistoryPixelInfo(Long pixelId, Long userId) {
		Pixel pixel = pixelRepository.findById(pixelId)
			.orElseThrow(() -> new AppException(ErrorCode.PIXEL_NOT_FOUND));

		User user = userRepository.getReferenceById(userId);

		List<LocalDateTime> visitList = pixelUserRepository.findAllVisitHistoryByPixelAndUser(pixel, user).stream()
			.map(BaseTimeEntity::getCreatedAt)
			.toList();

		return new IndividualHistoryPixelInfoResponse(pixel.getAddress(), pixel.getAddressNumber(), visitList.size(),
			visitList);
	}

	public PixelCountResponse getPixelCount(Long userId) {
		return PixelCountResponse.builder()
			.currentPixelCount(rankingService.getCurrentPixelCount(userId))
			.accumulatePixelCount(pixelUserRepository.countAccumulatePixelByUserId(userId))
			.build();
	}

	private PixelOwnerUserResponse getPixelOwnerUserInfo(Pixel pixel) {
		Long ownerUserId = pixel.getUserId();
		if (ownerUserId == null) {
			return null;
		} else {
			Long accumulatePixelCount = pixelUserRepository.countAccumulatePixelByUserId(ownerUserId);
			Long currentPixelCount = rankingService.getCurrentPixelCount(ownerUserId);
			User ownerUser = userRepository.findById(ownerUserId)
				.orElseThrow(() -> {
					log.error("pixel {} 의 소유자가 {} 인데 존재하지 않음.", pixel.getId(), ownerUserId);
					return new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
				});
			return PixelOwnerUserResponse.from(ownerUser, currentPixelCount, accumulatePixelCount);
		}
	}
}
