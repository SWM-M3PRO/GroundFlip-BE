package com.m3pro.groundflip.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.m3pro.groundflip.domain.dto.pixel.*;
import com.m3pro.groundflip.domain.dto.pixel.NaverAPI.NaverAPIResult;
// import com.m3pro.groundflip.domain.dto.pixel.NaverAPI.Result;
import com.m3pro.groundflip.domain.dto.pixelUser.IndividualHistoryPixelInfoResponse;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.domain.entity.global.BaseTimeEntity;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.m3pro.groundflip.domain.dto.pixelUser.PixelCount;
import com.m3pro.groundflip.domain.dto.pixelUser.PixelOwnerUser;
import com.m3pro.groundflip.domain.dto.pixelUser.VisitedUser;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.domain.entity.PixelUser;
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

	public List<IndividualPixelResponse> getNearIndividualPixelsByCoordinate(double currentLatitude,
		double currentLongitude, int radius) {
		Point point = geometryFactory.createPoint(new Coordinate(currentLongitude, currentLatitude));
		point.setSRID(WGS84_SRID);

		return pixelRepository.findAllIndividualPixelsByCoordinate(point, radius).stream()
			.map(IndividualPixelResponse::from)
			.toList();
	}

	public List<IndividualHistoryPixelResponse> getNearIndividualHistoryPixelsByCoordinate(double currentLatitude,
		double currentLongitude, int radius, Long userId) {
		Point point = geometryFactory.createPoint(new Coordinate(currentLongitude, currentLatitude));
		point.setSRID(WGS84_SRID);

		return pixelRepository.findAllIndividualPixelsHistoryByCoordinate(point, radius, userId).stream()
			.map(IndividualHistoryPixelResponse::from)
			.toList();
	}

	public IndividualPixelInfoResponse getIndividualPixelInfo(Long pixelId) {
		Optional<Pixel> pixel = pixelRepository.findById(pixelId);

		if (pixel.isEmpty()) {
			throw new AppException(ErrorCode.PIXEL_NOT_FOUND);
		}

		List<VisitedUser> visitedUsers = pixelUserRepository.findAllVisitedUserByPixelId(pixelId);
		PixelOwnerUserResponse pixelOwnerUserResponse = getPixelOwnerUserInfo(pixelId);

		return IndividualPixelInfoResponse.from(
			pixel.get(),
			pixelOwnerUserResponse,
			visitedUsers.stream().map(VisitedUserInfo::from).toList()
		);
	}

	private PixelOwnerUserResponse getPixelOwnerUserInfo(Long pixelId) {
		PixelOwnerUser pixelOwnerUser = pixelUserRepository.findCurrentOwnerByPixelId(pixelId);
		if (pixelOwnerUser == null) {
			return null;
		} else {
			PixelCount accumulatePixelCount = pixelUserRepository.findAccumulatePixelCountByUserId(
				pixelOwnerUser.getUserId());
			PixelCount currentPixelCount = pixelUserRepository.findCurrentPixelCountByUserId(
				pixelOwnerUser.getUserId());
			return PixelOwnerUserResponse.from(pixelOwnerUser, currentPixelCount, accumulatePixelCount);
		}
	}

	@Transactional
	public void occupyPixel(PixelOccupyRequest pixelOccupyRequest) {
		Long communityId = pixelOccupyRequest.getCommunityId();

		Pixel targetPixel = pixelRepository.findByXAndY(pixelOccupyRequest.getX(), pixelOccupyRequest.getY())
			.orElseThrow(() -> new AppException(ErrorCode.PIXEL_NOT_FOUND));

		if (pixelOccupyRequest.getCommunityId() == null) {
			communityId = -1L;
		}

		PixelUser pixelUser = PixelUser.builder()
			.pixel(targetPixel)
			.community(communityRepository.getReferenceById(communityId))
			.user(userRepository.getReferenceById(pixelOccupyRequest.getUserId()))
			.build();

		pixelUserRepository.save(pixelUser);
	}

	public IndividualHistoryPixelInfoResponse getIndividualHistoryPixelInfo(Long pixelId, Long userId) {
		Pixel pixel = pixelRepository.findById(pixelId)
			.orElseThrow(() -> new AppException(ErrorCode.PIXEL_NOT_FOUND));

		User user = userRepository.getReferenceById(userId);

		List<LocalDateTime> visitList = pixelUserRepository.findAllByPixelAndUserOrderByCreatedAt(pixel, user).stream()
			.map(BaseTimeEntity::getCreatedAt)
			.toList();

		return new IndividualHistoryPixelInfoResponse(pixel.getAddress(), pixel.getAddressNumber(), visitList.size(),
			visitList);
	}


	public NaverAPIResult getNaverAPIResult(double longitude, double latitude) {
		String coordinate = String.format("%f, %f", longitude,latitude);
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
			.header("X-NCP-APIGW-API-KEY-ID", "2acqb1fmzz")
			.header("X-NCP-APIGW-API-KEY", "ost9PcixvpADuc8AWUQGJWIhgnWo6vl0uLUMPw86")
			.build();

		ResponseEntity<NaverAPIResult> result = restTemplate.exchange(req, NaverAPIResult.class);

		System.out.println(result.getBody().getAreaNames());

		return result.getBody();
	}

}
