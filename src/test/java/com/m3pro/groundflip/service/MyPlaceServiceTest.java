package com.m3pro.groundflip.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.myplace.MyPlaceRequest;
import com.m3pro.groundflip.domain.entity.MyPlace;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.enums.Place;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.MyPlaceRepository;
import com.m3pro.groundflip.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class MyPlaceServiceTest {
	@Mock
	private UserRepository userRepository;

	@Mock
	private GeometryFactory geometryFactory;

	@Mock
	private MyPlaceRepository myPlaceRepository;

	@InjectMocks
	private MyPlaceService myPlaceService;

	private MyPlaceRequest myPlaceRequest;

	private User user;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		User user = User.builder().id(1L).nickname("test User").build();

	}

	@Test
	@DisplayName("[putMyPlace] 즐겨찾기 장소가 올바르게 업데이트 되는지")
	void putMyPlaceTest() {
		//Given

		myPlaceRequest = MyPlaceRequest.builder()
			.userId(1L)
			.placeName(Place.HOME)
			.latitude(37.321147)
			.longitude(127.093171)
			.build();
		//When
		when(userRepository.findById(myPlaceRequest.getUserId())).thenReturn(Optional.of(user));

		myPlaceService.putMyPlace(myPlaceRequest);
		//Then
		verify(userRepository, times(1)).findById(myPlaceRequest.getUserId());

		assertThat(user.getNickname()).isEqualTo("testUser");
	}

	@Test
	@DisplayName("[putMyPlace] 유저가 없을때 user not found에러가 잘 나오는지")
	void puyMyPlaceErrorTest() {
		myPlaceRequest = MyPlaceRequest.builder()
			.userId(1L)
			.placeName(Place.HOME)
			.latitude(37.321147)
			.longitude(127.093171)
			.build();

		when(userRepository.findById(myPlaceRequest.getUserId())).thenReturn(Optional.empty());

		AppException thrown = assertThrows(AppException.class, () -> {
			myPlaceService.putMyPlace(myPlaceRequest);
		});
		assertEquals(ErrorCode.USER_NOT_FOUND, thrown.getErrorCode());

	}

	@Test
	@DisplayName("[getMyPlace] 즐겨찾기 장소를 올바르게 가져오는지")
	void getMyPlaceTest() {
		//Given
		Long userId = 1L;
		double latitude1 = 37.321147;
		double latitude2 = 37.123456;
		double longitude1 = 127.093171;
		double longitude2 = 127.123456;

		Point point1 = geometryFactory.createPoint(new Coordinate(latitude1, longitude1));
		Point point2 = geometryFactory.createPoint(new Coordinate(latitude2, longitude2));

		List<MyPlace> myPlaceList = Arrays.asList(
			MyPlace.builder().placeName(Place.HOME).placePoint(point1).build(),
			MyPlace.builder().placeName(Place.HOME).placePoint(point2).build()
		);

		//When
		when(myPlaceRepository.findByUserId(userId)).thenReturn(myPlaceList);

		List<MyPlace> myPlaces = myPlaceRepository.findByUserId(userId);

		//Then
		assertEquals(2, myPlaces.size());
		assertEquals(point1, myPlaces.get(0).getPlacePoint());
		assertEquals(point2, myPlaces.get(1).getPlacePoint());
	}

	@Test
	@DisplayName("[getMyPlace] 저장된 값이 없을때 에러가 잘 나오는지")
	void getMyPlaceErrorTest() {
		//Given
		Long userId = 1L;

		//When
		AppException thrown = assertThrows(AppException.class, () -> {
			myPlaceService.getMyPlace(userId);
		});

		//When
		assertEquals(ErrorCode.PLACE_NOT_FOUND, thrown.getErrorCode());
	}
}
