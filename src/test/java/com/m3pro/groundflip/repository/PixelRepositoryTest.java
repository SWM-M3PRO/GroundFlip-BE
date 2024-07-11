package com.m3pro.groundflip.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.m3pro.groundflip.domain.entity.Pixel;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PixelRepositoryTest {
	private static final int WGS84_SRID = 4326;

	@Autowired
	PixelRepository pixelRepository;

	@Autowired
	GeometryFactory geometryFactory;

	@Test
	void save() {
		Point point = geometryFactory.createPoint(new Coordinate(124.231212, 37.213122));
		point.setSRID(WGS84_SRID);

		Pixel save = pixelRepository.save(Pixel.builder()
			.coordinate(point)
			.x(1L)
			.y(1L)
			.build());

		Assertions.assertThat(save.getCoordinate().getX() == point.getX());
	}
}
