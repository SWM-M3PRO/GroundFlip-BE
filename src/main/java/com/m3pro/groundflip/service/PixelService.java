package com.m3pro.groundflip.service;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.pixel.IndividualPixelResponse;
import com.m3pro.groundflip.repository.PixelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PixelService {
	private final GeometryFactory geometryFactory;
	private final PixelRepository pixelRepository;
	private static final int WGS84_SRID = 4326;

	public List<IndividualPixelResponse> getNearIndividualPixelsByCoordinate(double currentLatitude,
		double currentLongitude, int radius) {
		Point point = geometryFactory.createPoint(new Coordinate(currentLongitude, currentLatitude));
		point.setSRID(WGS84_SRID);

		return pixelRepository.findAllIndividualPixelsByCoordinate(point, radius).stream()
			.map(IndividualPixelResponse::from)
			.toList();
	}
}
