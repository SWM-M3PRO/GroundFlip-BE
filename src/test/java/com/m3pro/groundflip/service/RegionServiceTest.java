package com.m3pro.groundflip.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.pixel.ClusteredPixelCount;
import com.m3pro.groundflip.repository.RegionRepository;

@ExtendWith(MockitoExtension.class)
class RegionServiceTest {
	private static final double lat_per_pixel = 0.000724;
	private static final double lon_per_pixel = 0.000909;
	private static final double upper_left_lat = 38.240675;
	private static final double upper_left_lon = 125.905952;
	private static final int WGS84_SRID = 4326;

	@Mock
	private RegionRepository regionRepository;
	@Mock
	private GeometryFactory geometryFactory;
	@InjectMocks
	private RegionService regionService;

	@Test
	@DisplayName("[getIndividualModeClusteredPixelCount] City 레벨의 클러스터링 집계 결과를 반환한다")
	void testGetIndividualModeClusteredPixelCountCity() {
		double expectedLongitude = upper_left_lon + (233L * lon_per_pixel);
		double expectedLatitude = upper_left_lat - (213L * lat_per_pixel);
		Coordinate coordinate = new Coordinate(expectedLongitude, expectedLatitude);
		Point mockPoint = new GeometryFactory().createPoint(coordinate);
		mockPoint.setSRID(WGS84_SRID);
		when(geometryFactory.createPoint(any(Coordinate.class))).thenReturn(mockPoint);

		List<ClusteredPixelCount> result = regionService.getIndividualModeClusteredPixelCount(expectedLatitude,
			expectedLongitude, 6000);

		verify(regionRepository, times(1)).findAllIndividualCityRegionsByCoordinate(any(), anyInt(), anyInt(),
			anyInt());
	}

	@Test
	@DisplayName("[getIndividualModeClusteredPixelCount] Province 레벨의 클러스터링 집계 결과를 반환한다")
	void testGetIndividualModeClusteredPixelCountProvince() {
		double expectedLongitude = upper_left_lon + (233L * lon_per_pixel);
		double expectedLatitude = upper_left_lat - (213L * lat_per_pixel);
		Coordinate coordinate = new Coordinate(expectedLongitude, expectedLatitude);
		Point mockPoint = new GeometryFactory().createPoint(coordinate);
		mockPoint.setSRID(WGS84_SRID);

		when(geometryFactory.createPoint(any(Coordinate.class))).thenReturn(mockPoint);

		regionService.getIndividualModeClusteredPixelCount(expectedLatitude,
			expectedLongitude, 70001);

		verify(regionRepository, times(1)).findAllIndividualProvinceRegionsByCoordinate(anyInt(), anyInt());
	}

	@Test
	@DisplayName("[getCommunityModeClusteredPixelCount] City 레벨의 클러스터링 집계 결과를 반환한다")
	void testGetCommunityModeClusteredPixelCountCity() {
		double expectedLongitude = upper_left_lon + (233L * lon_per_pixel);
		double expectedLatitude = upper_left_lat - (213L * lat_per_pixel);
		Coordinate coordinate = new Coordinate(expectedLongitude, expectedLatitude);
		Point mockPoint = new GeometryFactory().createPoint(coordinate);
		mockPoint.setSRID(WGS84_SRID);

		when(geometryFactory.createPoint(any(Coordinate.class))).thenReturn(mockPoint);

		regionService.getCommunityModeClusteredPixelCount(expectedLatitude,
			expectedLongitude, 6000);

		verify(regionRepository, times(1)).findAllCommunityCityRegionsByCoordinate(any(), anyInt(), anyInt(), anyInt());
	}

	@Test
	@DisplayName("[getCommunityModeClusteredPixelCount] Province 레벨의 클러스터링 집계 결과를 반환한다")
	void testGetCommunityModeClusteredPixelCountProvince() {
		double expectedLongitude = upper_left_lon + (233L * lon_per_pixel);
		double expectedLatitude = upper_left_lat - (213L * lat_per_pixel);
		Coordinate coordinate = new Coordinate(expectedLongitude, expectedLatitude);
		Point mockPoint = new GeometryFactory().createPoint(coordinate);
		mockPoint.setSRID(WGS84_SRID);

		when(geometryFactory.createPoint(any(Coordinate.class))).thenReturn(mockPoint);

		regionService.getCommunityModeClusteredPixelCount(expectedLatitude,
			expectedLongitude, 70001);

		verify(regionRepository, times(1)).findAllCommunityProvinceRegionsByCoordinate(anyInt(), anyInt());
	}

	@Test
	@DisplayName("[getIndividualHistoryClusteredPixelCount] City 레벨의 클러스터링 집계 결과를 반환한다")
	void testGetIndividualHistoryClusteredPixelCountCity() {
		double expectedLongitude = upper_left_lon + (233L * lon_per_pixel);
		double expectedLatitude = upper_left_lat - (213L * lat_per_pixel);
		Coordinate coordinate = new Coordinate(expectedLongitude, expectedLatitude);
		Point mockPoint = new GeometryFactory().createPoint(coordinate);
		mockPoint.setSRID(WGS84_SRID);

		when(geometryFactory.createPoint(any(Coordinate.class))).thenReturn(mockPoint);

		regionService.getIndividualHistoryClusteredPixelCount(expectedLatitude,
			expectedLongitude, 6000, 1L);

		verify(regionRepository, times(1)).findAllIndividualHistoryCityRegionsByCoordinate(any(), anyInt(), any());
	}

	@Test
	@DisplayName("[getIndividualHistoryClusteredPixelCount] Province 레벨의 클러스터링 집계 결과를 반환한다")
	void testGetIndividualHistoryClusteredPixelCountProvince() {
		double expectedLongitude = upper_left_lon + (233L * lon_per_pixel);
		double expectedLatitude = upper_left_lat - (213L * lat_per_pixel);
		Coordinate coordinate = new Coordinate(expectedLongitude, expectedLatitude);
		Point mockPoint = new GeometryFactory().createPoint(coordinate);
		mockPoint.setSRID(WGS84_SRID);

		when(geometryFactory.createPoint(any(Coordinate.class))).thenReturn(mockPoint);

		regionService.getIndividualHistoryClusteredPixelCount(expectedLatitude,
			expectedLongitude, 70001, 1L);

		verify(regionRepository, times(1)).findAllIndividualHistoryProvinceRegionsByCoordinate(any());
	}
}