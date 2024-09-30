package com.m3pro.groundflip.service;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.pixel.ClusteredPixelCount;
import com.m3pro.groundflip.domain.dto.pixel.RegionInfo;
import com.m3pro.groundflip.enums.RegionLevel;
import com.m3pro.groundflip.repository.RegionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegionService {
	private static final int WGS84_SRID = 4326;
	private static final int CITY_LEVEL_THRESHOLD = 70000;
	private final RegionRepository regionRepository;
	private final GeometryFactory geometryFactory;

	public List<ClusteredPixelCount> getIndividualModeClusteredPixelCount(
		double currentLatitude,
		double currentLongitude,
		int radius
	) {
		Point point = geometryFactory.createPoint(new Coordinate(currentLongitude, currentLatitude));
		point.setSRID(WGS84_SRID);
		RegionLevel regionLevel = radius < CITY_LEVEL_THRESHOLD ? RegionLevel.CITY : RegionLevel.PROVINCE;

		List<RegionInfo> regions = regionRepository.findAllCityRegionsByCoordinate(point, radius, regionLevel);
		return regions.stream().map(region -> ClusteredPixelCount.from(
			region.getRegionId(),
			region.getName(),
			(int)(Math.random() * 1500) + 1,
			region.getLatitude(),
			region.getLongitude(),
			regionLevel
		)).toList();
	}
}
