package com.m3pro.groundflip.repository;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.m3pro.groundflip.domain.entity.Community;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.domain.entity.PixelUser;
import com.m3pro.groundflip.domain.entity.User;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PixelUserRepositoryTest {
	private static final int WGS84_SRID = 4326;

	@Autowired
	private PixelUserRepository pixelUserRepository;
	@Autowired
	private PixelRepository pixelRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	CommunityRepository communityRepository;
	@Autowired
	GeometryFactory geometryFactory;

	@Test
	@Transactional
	@DisplayName("[save] pixel_user가 정상적으로 삽입되는지 확인")
	void save() {
		User savedUser = userRepository.save(
			User.builder()
				.build()
		);

		Pixel savedPixel = pixelRepository.save(Pixel.builder()
			.coordinate(createPoint(37.0, 127.0))
			.build());

		Community savedCommunity = communityRepository.save(
			Community.builder()
				.build()
		);

		pixelUserRepository.save(savedPixel.getId(), savedUser.getId(), savedCommunity.getId());

		List<PixelUser> pixelUsers = pixelUserRepository.findAll();

		Assertions.assertThat(pixelUsers.size()).isEqualTo(1);
	}

	private Point createPoint(double longitude, double latitude) {
		Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
		point.setSRID(WGS84_SRID);
		return point;
	}
}
