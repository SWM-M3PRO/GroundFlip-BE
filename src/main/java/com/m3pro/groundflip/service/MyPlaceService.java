package com.m3pro.groundflip.service;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.myplace.MyPlaceRequest;
import com.m3pro.groundflip.domain.dto.myplace.MyPlaceResponse;
import com.m3pro.groundflip.domain.entity.MyPlace;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.MyPlaceRepository;
import com.m3pro.groundflip.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPlaceService {

	private final UserRepository userRepository;
	private final GeometryFactory geometryFactory;
	private final MyPlaceRepository myPlaceRepository;

	@Transactional
	public void putMyPlace(MyPlaceRequest myPlaceRequest) {
		User user = userRepository.findById(myPlaceRequest.getUserId())
			.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		double placeLongitude = myPlaceRequest.getLongitude();
		double placeLatitude = myPlaceRequest.getLatitude();

		Point point = geometryFactory.createPoint(new Coordinate(placeLongitude, placeLatitude));

		myPlaceRepository.save(
			MyPlace.builder()
				.user(user)
				.placePoint(point)
				.placeName(myPlaceRequest.getPlaceName())
				.build()
		);
	}

	public List<MyPlaceResponse> getMyPlace(Long userId) {
		List<MyPlace> myPlaces = myPlaceRepository.findByUserId(userId);
		return myPlaces.stream().map(MyPlaceResponse::from).toList();
	}

	public void deleteMyPlace(MyPlaceRequest myPlaceRequest) {
		List<MyPlace> myplaces = myPlaceRepository.findByUserIdAndPlaceName(myPlaceRequest.getUserId(),
			myPlaceRequest.getPlaceName());
		if (myplaces.isEmpty()) {
			throw new AppException(ErrorCode.PLACE_NOT_FOUND);
		}
		myPlaceRepository.deleteAll(myplaces);
	}

}
