package com.m3pro.groundflip.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m3pro.groundflip.domain.dto.pixel.PixelOccupyRequest;
import com.m3pro.groundflip.domain.dto.pixel.naverApi.ReverseGeocodingResult;
import com.m3pro.groundflip.domain.entity.CompetitionCount;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.domain.entity.PixelUser;
import com.m3pro.groundflip.domain.entity.Region;
import com.m3pro.groundflip.domain.entity.UserRegionCount;
import com.m3pro.groundflip.enums.AchievementCategoryId;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.CommunityRepository;
import com.m3pro.groundflip.repository.CompetitionCountRepository;
import com.m3pro.groundflip.repository.PixelRepository;
import com.m3pro.groundflip.repository.PixelUserRepository;
import com.m3pro.groundflip.repository.RegionRepository;
import com.m3pro.groundflip.repository.UserRegionCountRepository;
import com.m3pro.groundflip.repository.UserRepository;
import com.m3pro.groundflip.util.DateUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PixelManager {
	private static final Long DEFAULT_COMMUNITY_ID = -1L;
	private static final int WGS84_SRID = 4326;
	private static final double lat_per_pixel = 0.000724;
	private static final double lon_per_pixel = 0.000909;
	private static final double upper_left_lat = 38.240675;
	private static final double upper_left_lon = 125.905952;

	private final PixelRepository pixelRepository;
	private final UserRankingService userRankingService;
	private final CommunityRankingService communityRankingService;
	private final PixelUserRepository pixelUserRepository;
	private final GeometryFactory geometryFactory;
	private final ReverseGeoCodingService reverseGeoCodingService;
	private final RegionRepository regionRepository;
	private final CompetitionCountRepository competitionCountRepository;
	private final UserRegionCountRepository userRegionCountRepository;
	private final UserRepository userRepository;
	private final CommunityRepository communityRepository;
	private final AchievementManager achievementManager;

	@Transactional
	public void occupyPixel(PixelOccupyRequest pixelOccupyRequest) {
		Long occupyingUserId = pixelOccupyRequest.getUserId();
		Long occupyingCommunityId = Optional.ofNullable(pixelOccupyRequest.getCommunityId()).orElse(-1L);
		log.info("[Visit Pixel] x : {}, y: {}, user : {}", pixelOccupyRequest.getX(), pixelOccupyRequest.getY(),
			occupyingUserId);

		if (!isValidCoordinate(pixelOccupyRequest.getX(), pixelOccupyRequest.getY())) {
			throw new AppException(ErrorCode.PIXEL_NOT_FOUND);
		}

		Pixel targetPixel = pixelRepository.findByXAndY(pixelOccupyRequest.getX(),
				pixelOccupyRequest.getY())
			.orElseGet(() -> createPixel(pixelOccupyRequest.getX(), pixelOccupyRequest.getY()));
		updateRegionCount(targetPixel, occupyingCommunityId != -1L);
		userRankingService.updateCurrentPixelRanking(targetPixel, occupyingUserId);
		updateUserAccumulatePixelCount(targetPixel, occupyingUserId);
		updatePixelOwnerUser(targetPixel, occupyingUserId);

		updateCommunityCurrentPixelCount(targetPixel, occupyingCommunityId);
		updateCommunityAccumulatePixelCount(targetPixel, occupyingCommunityId);
		updatePixelOwnerCommunity(targetPixel, occupyingCommunityId);

		savePixelUser(targetPixel, occupyingUserId, occupyingCommunityId);
	}

	private void savePixelUser(Pixel targetPixel, Long occupyingUserId, Long occupyingCommunityId) {
		PixelUser pixelUser = PixelUser.builder()
			.pixel(targetPixel)
			.user(userRepository.getReferenceById(occupyingUserId))
			.community(communityRepository.getReferenceById(occupyingCommunityId))
			.build();
		pixelUserRepository.save(pixelUser);
	}

	private boolean isValidCoordinate(Long x, Long y) {
		return x >= 0 && x < 9000 && y >= 0 && y < 8156;
	}

	private Pixel createPixel(Long x, Long y) {
		Long pixelId = getPixelId(x, y);
		Point coordinate = getCoordinate(x, y);
		ReverseGeocodingResult reverseGeocodingResult = getRegion(coordinate);
		log.info("x: {}, y: {} pixel 생성", x, y);
		Region region = reverseGeocodingResult.getRegionId() != null
			? regionRepository.getReferenceById(reverseGeocodingResult.getRegionId()) : null;

		Pixel pixel = Pixel.builder()
			.id(pixelId)
			.x(x)
			.y(y)
			.coordinate(coordinate)
			.createdAt(LocalDateTime.now())
			.userOccupiedAt(LocalDateTime.of(2024, 6, 1, 0, 0))
			.communityOccupiedAt(LocalDateTime.of(2024, 6, 1, 0, 0))
			.region(region)
			.address(reverseGeocodingResult.getRegionName())
			.build();
		return pixelRepository.save(pixel);
	}

	private ReverseGeocodingResult getRegion(Point coordinate) {
		double longitude = coordinate.getX();
		double latitude = coordinate.getY();
		try {
			return reverseGeoCodingService.getRegionFromCoordinates(longitude, latitude);
		} catch (Exception e) {
			String errorLog = "[Reverse Geocoding Error] longitude : " + longitude + ", latitude : " + latitude + "  ";
			log.error("{}{}", errorLog, e.getMessage(), e);
			return ReverseGeocodingResult.builder().regionId(null).regionName(null).build();
		}
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
			updateUserRegionCount(targetPixel, userId);
			userRankingService.updateAccumulatedRanking(userId);
			achievementManager.updateAccumulateAchievement(userId, AchievementCategoryId.EXPLORER);
		}
		if (!pixelUserRepository.existsByUserIdAndPixelIdForToday(userId, LocalDateTime.now())) {
			achievementManager.updateAccumulateAchievement(userId, AchievementCategoryId.STEADY);
		}
	}

	private void updateUserRegionCount(Pixel targetPixel, Long userId) {
		if (targetPixel.getRegion() == null) {
			return;
		}
		UserRegionCount userRegionCount = userRegionCountRepository
			.findByRegionAndUser(targetPixel.getRegion(), userId)
			.orElseGet(() -> createUserRegionCount(targetPixel.getRegion(), userId));
		userRegionCount.increaseCount();
	}

	private UserRegionCount createUserRegionCount(Region region, Long userId) {
		UserRegionCount userRegionCount = UserRegionCount.builder()
			.count(0)
			.region(region)
			.user(userRepository.getReferenceById(userId))
			.build();
		return userRegionCountRepository.save(userRegionCount);
	}

	private void updateCommunityCurrentPixelCount(Pixel targetPixel, Long communityId) {
		if (!communityId.equals(DEFAULT_COMMUNITY_ID)) {
			communityRankingService.updateCurrentPixelRanking(targetPixel, communityId);
		}
	}

	private void updatePixelOwnerUser(Pixel targetPixel, Long occupyingUserId) {
		if (isLandTakenFromExistingUser(targetPixel, occupyingUserId)) {
			achievementManager.updateAccumulateAchievement(occupyingUserId, AchievementCategoryId.CONQUEROR);
		}
		targetPixel.updateUserId(occupyingUserId);
		targetPixel.updateUserOccupiedAtToNow();
	}

	private boolean isLandTakenFromExistingUser(Pixel targetPixel, Long occupyingUserId) {
		Long originalOwnerUserId = targetPixel.getUserId();
		LocalDateTime thisWeekStart = DateUtils.getThisWeekStartDate().atTime(0, 0);
		LocalDateTime userOccupiedAt = targetPixel.getUserOccupiedAt();
		if (!Objects.equals(originalOwnerUserId, occupyingUserId)) {
			return originalOwnerUserId != null && !userOccupiedAt.isBefore(thisWeekStart);
		} else {
			return false;
		}

	}

	private void updateRegionCount(Pixel targetPixel, boolean isCommunityUpdatable) {
		if (targetPixel.getRegion() != null) {
			LocalDate now = LocalDate.now();
			int week = DateUtils.getWeekOfDate(now);
			int year = now.getYear();
			CompetitionCount competitionCount = competitionCountRepository
				.findByRegion(targetPixel.getRegion(), week, year)
				.orElseGet(() -> createCompetitionCount(targetPixel.getRegion(), week, year));
			if (!DateUtils.isDateInCurrentWeek(targetPixel.getUserOccupiedAt().toLocalDate())) {
				competitionCount.increaseIndividualModeCount();
			}
			if (isCommunityUpdatable && !DateUtils.isDateInCurrentWeek(
				targetPixel.getCommunityOccupiedAt().toLocalDate())) {
				competitionCount.increaseCommunityModeCount();
			}
		}
	}

	private CompetitionCount createCompetitionCount(Region region, int week, int year) {
		CompetitionCount competitionCount = CompetitionCount.builder()
			.individualModeCount(0)
			.communityModeCount(0)
			.region(region)
			.week(week)
			.year(year)
			.build();
		return competitionCountRepository.save(competitionCount);
	}

	private void updatePixelOwnerCommunity(Pixel targetPixel, Long occupyingCommunityId) {
		if (!occupyingCommunityId.equals(DEFAULT_COMMUNITY_ID)) {
			targetPixel.updateCommunityId(occupyingCommunityId);
			targetPixel.updateCommunityOccupiedAtToNow();
		}
	}
}
