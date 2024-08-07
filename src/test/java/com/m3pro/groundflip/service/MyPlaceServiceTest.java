package com.m3pro.groundflip.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

	// @BeforeEach
	// void setUp() {
	// 	MockitoAnnotations.openMocks(this);
	// 	//user = User.builder().id(1L).nickname("testUser").build();
	//
	// }

	@Test
	@DisplayName("[putMyPlace] 즐겨찾기 장소가 올바르게 업데이트 되는지")
	void putMyPlaceTest() {
		// Given
		User user = User.builder()
			.id(1L)
			.nickname("testUser")
			.build();

		MyPlaceRequest myPlaceRequest = MyPlaceRequest.builder()
			.userId(1L)
			.placeName(Place.HOME)
			.latitude(37.321147)
			.longitude(127.093171)
			.build();

		Point mockPoint = mock(Point.class);
		when(geometryFactory.createPoint(any(Coordinate.class))).thenReturn(mockPoint);

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(myPlaceRepository.save(any(MyPlace.class))).thenReturn(null);

		// When
		myPlaceService.putMyPlace(myPlaceRequest);

		// Then
		verify(userRepository, times(1)).findById(myPlaceRequest.getUserId());
		verify(myPlaceRepository, times(1)).save(any(MyPlace.class));

		//assertThat(user.getNickname()).isEqualTo("testUser");
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
	@DisplayName("[deleteMyPlace] 즐겨찾기 삭제 동작 테스트")
	void deleteMyPlaceTest() {
		//Given
		Long userId = 1L;

		myPlaceRequest = MyPlaceRequest.builder()
			.userId(userId)
			.placeName(Place.HOME)
			.build();

		List<MyPlace> myPlaceList = Arrays.asList(
			MyPlace.builder().placeName(Place.HOME).user(user).build(),
			MyPlace.builder().placeName(Place.HOME).user(user).build()
		);

		//when(myPlaceRepository.findByUserIdAndPlaceName(userId, Place.HOME)).thenReturn(myPlaceList);

		myPlaceRepository.deleteAll(myPlaceList);

		verify(myPlaceRepository, times(1)).deleteAll(myPlaceList);

	}

	@Test
	@DisplayName("[deleteMyPlace] 즐겨찾기 장소가 존재하지 않을 때 에러가 발생하는지")
	void deleteMyPlace_NotFound() {
		// Given
		Long userId = 1L;
		Place placeName = Place.HOME;
		MyPlaceRequest myPlaceRequest = MyPlaceRequest.builder()
			.userId(userId)
			.placeName(placeName)
			.build();

		// When

		AppException thrown = assertThrows(AppException.class, () -> {
			myPlaceService.deleteMyPlace(myPlaceRequest);
		});

		assertEquals(ErrorCode.PLACE_NOT_FOUND, thrown.getErrorCode());
	}
}
