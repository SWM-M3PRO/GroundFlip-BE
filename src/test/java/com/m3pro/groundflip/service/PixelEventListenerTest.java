package com.m3pro.groundflip.service;

import static org.mockito.Mockito.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.pixel.event.PixelAddressUpdateEvent;
import com.m3pro.groundflip.domain.dto.pixel.event.PixelUserInsertEvent;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.repository.PixelRepository;
import com.m3pro.groundflip.repository.PixelUserRepository;

@ExtendWith(MockitoExtension.class)
public class PixelEventListenerTest {
	private static final int WGS84_SRID = 4326;

	@Mock
	private PixelUserRepository pixelUserRepository;
	@Mock
	private ReverseGeoCodingService reverseGeoCodingService;
	@Mock
	private PixelRepository pixelRepository;
	@InjectMocks
	private PixelEventListener pixelEventListener;

	@Test
	@DisplayName("[insertPixelUserHistory] save 메소드가 정상적으로 실행되는지 확인")
	public void insertPixelUserHistoryTest() {
		pixelEventListener.insertPixelUserHistory(new PixelUserInsertEvent(1L, 1L, -1L));

		verify(pixelUserRepository, times(1)).save(1L, 1L, -1L);
	}

	@Test
	@DisplayName("[updatePixelAddress] 픽셀 주소가 업데이트 되는 지 확인")
	public void updatePixelAddressTest() {
		Pixel pixel = Pixel.builder()
			.coordinate(createPoint(127.0, 37.0))
			.address(null)
			.build();

		when(reverseGeoCodingService.getAddressFromCoordinates(127.0, 37.0))
			.thenReturn("서울시 은평구");

		pixelEventListener.updatePixelAddress(new PixelAddressUpdateEvent(
			pixel
		));

		Assertions.assertThat(pixel.getAddress()).isEqualTo("서울시 은평구");
	}

	private Point createPoint(double longitude, double latitude) {
		GeometryFactory geometryFactory = new GeometryFactory();
		Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
		point.setSRID(WGS84_SRID);
		return point;
	}
}
