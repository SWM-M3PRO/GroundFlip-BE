package com.m3pro.groundflip.service;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m3pro.groundflip.domain.dto.pixel.IndividualPixelResponse;
import com.m3pro.groundflip.domain.dto.pixel.PixelOccupyRequest;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.domain.entity.PixelUser;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.CommunityRepository;
import com.m3pro.groundflip.repository.PixelRepository;
import com.m3pro.groundflip.repository.PixelUserRepository;
import com.m3pro.groundflip.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PixelService {
	private final GeometryFactory geometryFactory;

	private final PixelRepository pixelRepository;

	private final PixelUserRepository pixelUserRepository;

	private final CommunityRepository communityRepository;

	private final UserRepository userRepository;

	private static final int WGS84_SRID = 4326;

	public List<IndividualPixelResponse> getNearIndividualPixelsByCoordinate(double currentLatitude,
		double currentLongitude, int radius) {
		Point point = geometryFactory.createPoint(new Coordinate(currentLongitude, currentLatitude));
		point.setSRID(WGS84_SRID);

		return pixelRepository.findAllIndividualPixelsByCoordinate(point, radius).stream()
			.map(IndividualPixelResponse::from)
			.toList();
	}

	@Transactional
	public void occupyPixel(PixelOccupyRequest pixelOccupyRequest) {
		Long communityId = pixelOccupyRequest.getCommunityId();

		Pixel targetPixel = pixelRepository.findById(pixelOccupyRequest.getPixelId())
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
}
