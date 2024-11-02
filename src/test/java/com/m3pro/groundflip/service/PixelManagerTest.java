package com.m3pro.groundflip.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.m3pro.groundflip.domain.dto.pixel.PixelOccupyRequest;
import com.m3pro.groundflip.domain.dto.pixel.event.PixelAddressUpdateEvent;
import com.m3pro.groundflip.domain.dto.pixel.naverApi.ReverseGeocodingResult;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.CommunityRepository;
import com.m3pro.groundflip.repository.PixelRepository;
import com.m3pro.groundflip.repository.PixelUserRepository;
import com.m3pro.groundflip.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PixelManagerTest {
	private static final double lat_per_pixel = 0.000724;
	private static final double lon_per_pixel = 0.000909;
	private static final double upper_left_lat = 38.240675;
	private static final double upper_left_lon = 125.905952;
	private static final int WGS84_SRID = 4326;
	@Mock
	private UserRepository userRepository;
	@Mock
	private CommunityRepository communityRepository;
	@Mock
	private AchievementManager achievementManager;
	@Mock
	private PixelRepository pixelRepository;
	@Mock
	private PixelUserRepository pixelUserRepository;
	@Mock
	private ApplicationEventPublisher applicationEventPublisher;
	@Mock
	private UserRankingService userRankingService;
	@Mock
	private CommunityRankingService communityRankingService;
	@Mock
	private GeometryFactory geometryFactory;
	@Mock
	private ReverseGeoCodingService reverseGeoCodingService;
	@InjectMocks
	private PixelManager pixelManager;

	@Test
	@DisplayName("[occupyPixel] 픽셀을 정상적으로 차지하여 타겟 픽셀의 userId가 바뀐다.")
	void occupyPixel() {
		// Given
		PixelOccupyRequest pixelOccupyRequest = new PixelOccupyRequest(5L, 78611L, 222L, 233L);
		Pixel pixel = Pixel.builder()
			.x(222L)
			.y(233L)
			.userId(1L)
			.userOccupiedAt(LocalDateTime.now())
			.address("대한민국")
			.build();
		when(pixelRepository.findByXAndY(222L, 233L)).thenReturn(Optional.of(pixel));
		when(pixelUserRepository.existsByPixelIdAndUserId(any(), any())).thenReturn(false);
		// When
		pixelManager.occupyPixel(pixelOccupyRequest);

		//Then
		verify(userRankingService, times(1)).updateCurrentPixelRanking(any(), any());
		verify(userRankingService, times(1)).updateAccumulatedRanking(any());
		verify(communityRankingService, times(1)).updateCurrentPixelRanking(any(), any());
		assertEquals(5L, pixel.getUserId());
	}

	@Test
	@DisplayName("[occupyPixel] 픽셀을 차지할 때 PixelUserInsertEvent가 발행되는지 확인")
	void pixelUserInsertEventPublish() {
		PixelOccupyRequest pixelOccupyRequest = new PixelOccupyRequest(5L, 78611L, 222L, 233L);
		Pixel pixel = Pixel.builder()
			.x(222L)
			.y(233L)
			.userId(1L)
			.userOccupiedAt(LocalDateTime.now())
			.address("대한민국")
			.build();
		when(pixelRepository.findByXAndY(222L, 233L)).thenReturn(Optional.of(pixel));
		// When
		pixelManager.occupyPixel(pixelOccupyRequest);

		// Then
		verify(userRankingService, times(1)).updateAccumulatedRanking(any());
		verify(communityRankingService, times(1)).updateCurrentPixelRanking(any(), any());
		verify(pixelUserRepository, times(1)).save(any());
	}

	@Test
	@DisplayName("[occupyPixel] 픽셀을 차지할 때 주소가 null이면 PixelAddressUpdate 이벤트가 발행되는지 확인")
	void pixelAddressUpdateEventPublish() {
		PixelOccupyRequest pixelOccupyRequest = new PixelOccupyRequest(5L, 78611L, 222L, 233L);
		Pixel pixel = Pixel.builder()
			.x(222L)
			.y(233L)
			.userId(1L)
			.userOccupiedAt(LocalDateTime.now())
			.address(null)
			.build();
		when(pixelRepository.findByXAndY(222L, 233L)).thenReturn(Optional.of(pixel));
		lenient().when(reverseGeoCodingService.getRegionFromCoordinates(Mockito.any(Double.class),
			Mockito.any(Double.class))).thenReturn(
			ReverseGeocodingResult.builder().regionId(null).regionName(null).build());
		// When
		pixelManager.occupyPixel(pixelOccupyRequest);

		// Then
		verify(userRankingService, times(1)).updateAccumulatedRanking(any());
		verify(communityRankingService, times(1)).updateCurrentPixelRanking(any(), any());
	}

	@Test
	@DisplayName("[occupyPixel] 픽셀을 차지할 때 주소가 null이 아니라면 PixelAddressUpdate 이벤트가 발행되지 않는지 확인")
	void pixelAddressUpdateEventNotPublish() {
		PixelOccupyRequest pixelOccupyRequest = new PixelOccupyRequest(5L, 78611L, 222L, 233L);
		Pixel pixel = Pixel.builder()
			.x(222L)
			.y(233L)
			.userId(1L)
			.userOccupiedAt(LocalDateTime.now())
			.address("대한민국 ")
			.build();
		when(pixelRepository.findByXAndY(222L, 233L)).thenReturn(Optional.of(pixel));
		// When
		pixelManager.occupyPixel(pixelOccupyRequest);

		// Then
		verify(applicationEventPublisher, times(0)).publishEvent(any(PixelAddressUpdateEvent.class));
		verify(userRankingService, times(1)).updateAccumulatedRanking(any());
		verify(communityRankingService, times(1)).updateCurrentPixelRanking(any(), any());
	}

	@Test
	@DisplayName("[occupyPixel] 대한민국에 속한 pixel 이 아니라면 에러 발생")
	void pixelIncorrectPixel() {
		PixelOccupyRequest pixelOccupyRequest = new PixelOccupyRequest(5L, 78611L, 9000L, 233L);

		// Then
		AppException exception = assertThrows(AppException.class,
			() -> pixelManager.occupyPixel(pixelOccupyRequest));
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PIXEL_NOT_FOUND);
	}

	@Test
	@DisplayName("[occupyPixel] pixel 테이블에 등록되지 않는 픽셀 이라면 새로 생성후 작업 진행")
	void pixelOccupyTestNotRegisteredPixel() {
		PixelOccupyRequest pixelOccupyRequest = new PixelOccupyRequest(5L, 78611L, 213L, 233L);
		Pixel pixel = Pixel.builder()
			.x(222L)
			.y(233L)
			.userId(1L)
			.userOccupiedAt(LocalDateTime.now())
			.address("대한민국 ")
			.build();
		double expectedLongitude = upper_left_lon + (233L * lon_per_pixel);
		double expectedLatitude = upper_left_lat - (213L * lat_per_pixel);

		Coordinate coordinate = new Coordinate(expectedLongitude, expectedLatitude);
		Point mockPoint = new GeometryFactory().createPoint(coordinate);
		mockPoint.setSRID(WGS84_SRID);

		when(geometryFactory.createPoint(any(Coordinate.class))).thenReturn(mockPoint);
		when(pixelRepository.save(any())).thenReturn(pixel);
		lenient().when(reverseGeoCodingService.getRegionFromCoordinates(Mockito.any(Double.class),
			Mockito.any(Double.class))).thenReturn(
			ReverseGeocodingResult.builder().regionId(null).regionName(null).build());
		// When
		pixelManager.occupyPixel(pixelOccupyRequest);

		// Then
		verify(pixelRepository, times(1)).save(any());
	}
}
