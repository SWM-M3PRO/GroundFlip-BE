package com.m3pro.groundflip.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.m3pro.groundflip.domain.dto.pixel.IndividualModePixelResponse;
import com.m3pro.groundflip.domain.entity.Pixel;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PixelRepositoryTest {
	private static final int WGS84_SRID = 4326;
	private static final double CENTER_LATITUDE = 37.602481;
	private static final double CENTER_LONGITUDE = 126.924875;
	private static final double LATITUDE_50M_AWAY = 37.602220;
	private static final double LONGITUDE_50M_AWAY = 126.925151;
	private static final double LATITUDE_200M_AWAY = 37.604058;
	private static final double LONGITUDE_200M_AWAY = 126.925948;
	private static final int RADIUS = 100;

	@Autowired
	PixelRepository pixelRepository;

	@Autowired
	GeometryFactory geometryFactory;

	@Test
	@DisplayName("[findAllIndividualModePixelsByCoordinate] 반경 내에 존재하는 픽셀만 불러오는 지 테스트")
	@Transactional
	void findAllIndividualModePixelsByCoordinateInRange() {
		Pixel pixelInRange = savePixel(LATITUDE_50M_AWAY, LONGITUDE_50M_AWAY, 0L, 0L, 1L);
		Pixel pixelOutOfRange = savePixel(LATITUDE_200M_AWAY, LONGITUDE_200M_AWAY, 0L, 1L, 1L);

		Point center = createPoint(CENTER_LONGITUDE, CENTER_LATITUDE);

		List<IndividualModePixelResponse> result = pixelRepository.findAllIndividualModePixelsByCoordinate(center,
			RADIUS);

		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0).getPixelId()).isEqualTo(pixelInRange.getId());
	}

	@Test
	@DisplayName("[findCurrentPixelCountByUserId] 자신의 현재 픽셀 개수를 정확히 가져오는지 확인")
	@Transactional
	void countCurrentPixelByUserIdIsCorrect() {
		savePixel(LATITUDE_50M_AWAY, LONGITUDE_50M_AWAY, 0L, 0L, 1L);
		savePixel(LATITUDE_50M_AWAY, LONGITUDE_50M_AWAY, 0L, 0L, 2L);
		savePixel(LATITUDE_50M_AWAY, LONGITUDE_50M_AWAY, 0L, 0L, 2L);
		savePixel(LATITUDE_50M_AWAY, LONGITUDE_50M_AWAY, 0L, 0L, null);

		Long pixelCount = pixelRepository.countCurrentPixelByUserId(1L);

		assertThat(pixelCount).isEqualTo(1);
	}

	@Test
	@DisplayName("[occupyPixel] 픽셀의 소유권 변화가 정확히 일어나는 지 확인")
	@Transactional
	void occupyPixelIsCorrect() {
		Pixel targetPixel = savePixel(LATITUDE_50M_AWAY, LONGITUDE_50M_AWAY, 0L, 0L, 1L);

		targetPixel.updateUserId(2L);
		pixelRepository.saveAndFlush(targetPixel);

		Optional<Pixel> resultPixel = pixelRepository.findById(targetPixel.getId());
		assertThat(resultPixel.get().getUserId()).isEqualTo(2L);
	}

	private Pixel savePixel(double latitude, double longitude, Long x, Long y, Long userId) {
		Point point = createPoint(longitude, latitude);
		return pixelRepository.save(Pixel.builder()
			.coordinate(point)
			.userId(userId)
			.x(x)
			.y(y)
			.build());
	}

	private Point createPoint(double longitude, double latitude) {
		Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
		point.setSRID(WGS84_SRID);
		return point;
	}
}
