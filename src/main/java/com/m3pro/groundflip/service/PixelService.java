package com.m3pro.groundflip.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.m3pro.groundflip.domain.dto.pixel.IndividualHistoryPixelResponse;
import com.m3pro.groundflip.domain.dto.pixel.IndividualModePixelResponse;
import com.m3pro.groundflip.domain.dto.pixel.IndividualPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixel.NaverAPI.NaverReverseGeoCodingApiResult;
import com.m3pro.groundflip.domain.dto.pixel.PixelCountResponse;
import com.m3pro.groundflip.domain.dto.pixel.PixelOccupyRequest;
import com.m3pro.groundflip.domain.dto.pixel.PixelOwnerUserResponse;
import com.m3pro.groundflip.domain.dto.pixel.VisitedUserInfo;
import com.m3pro.groundflip.domain.dto.pixelUser.IndividualHistoryPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixelUser.PixelCount;
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

	@Value("${naver.apiKeyId}")
	String apiKeyId;
	@Value("${naver.apiKey}")
	String apiKey;

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

	private PixelOwnerUserResponse getPixelOwnerUserInfo(Pixel pixel) {
		Long ownerUserId = pixel.getUserId();
		if (ownerUserId == null) {
			return null;
		} else {
			PixelCount accumulatePixelCount = pixelUserRepository.findAccumulatePixelCountByUserId(ownerUserId);
			PixelCount currentPixelCount = pixelRepository.findCurrentPixelCountByUserId(ownerUserId);
			User ownerUser = userRepository.findById(ownerUserId)
				.orElseThrow(() -> {
					log.error("pixel {} 의 소유자가 {} 인데 존재하지 않음.", pixel.getId(), ownerUserId);
					return new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
				});
			return PixelOwnerUserResponse.from(ownerUser, currentPixelCount, accumulatePixelCount);
		}
	}

	@Transactional
	public void occupyPixel(PixelOccupyRequest pixelOccupyRequest) {
		Long communityId = pixelOccupyRequest.getCommunityId();

		if (pixelOccupyRequest.getCommunityId() == null) {
			communityId = -1L;
		}

		Pixel targetPixel = pixelRepository.findByXAndY(pixelOccupyRequest.getX(), pixelOccupyRequest.getY())
			.orElseThrow(() -> new AppException(ErrorCode.PIXEL_NOT_FOUND));

		updatePixelAddress(targetPixel);

		targetPixel.updateUserId(pixelOccupyRequest.getUserId());

		PixelUser pixelUser = PixelUser.builder()
			.pixel(targetPixel)
			.community(communityRepository.getReferenceById(communityId))
			.user(userRepository.getReferenceById(pixelOccupyRequest.getUserId()))
			.build();

		pixelUserRepository.save(pixelUser);
	}

	public void updatePixelAddress(Pixel targetPixel) {
		if (targetPixel.getAddress() == null) {
			List<String> naverAPIResult = getNaverAPIResult(targetPixel.getCoordinate().getX(),
				targetPixel.getCoordinate().getY());
			String address = String.join(" ", naverAPIResult);
			targetPixel.updateAddress(address);
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

	public List<String> getNaverAPIResult(double longitude, double latitude) {
		String coordinate = String.format("%f, %f", longitude, latitude);
		String url = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc";

		RestTemplate restTemplate = new RestTemplate();

		URI uri = UriComponentsBuilder.fromHttpUrl(url)
			.queryParam("coords", coordinate)
			.queryParam("orders", "admcode")
			.queryParam("output", "json")
			.encode(StandardCharsets.UTF_8)
			.build()
			.toUri();

		RequestEntity<Void> req = RequestEntity
			.get(uri)
			.header("X-NCP-APIGW-API-KEY-ID", apiKeyId)
			.header("X-NCP-APIGW-API-KEY", apiKey)
			.build();

		ResponseEntity<NaverReverseGeoCodingApiResult> result = restTemplate.exchange(req,
			NaverReverseGeoCodingApiResult.class);

		NaverReverseGeoCodingApiResult body = result.getBody();
		if (body != null) {
			return result.getBody().getAreaNames();
		} else {
			return null;
		}
	}

	public PixelCountResponse getPixelCount(Long userId) {
		return PixelCountResponse.builder()
			.currentPixelCount(pixelRepository.findCurrentPixelCountByUserId(userId).getCount())
			.accumulatePixelCount(pixelUserRepository.findAccumulatePixelCountByUserId(userId).getCount())
			.build();
	}
}
