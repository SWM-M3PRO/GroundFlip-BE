package com.m3pro.groundflip.domain.entity;

import org.locationtech.jts.geom.Point;

import com.m3pro.groundflip.enums.Place;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "my_place")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MyPlace {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "my_place_id")
	private Long id;

	@Enumerated(EnumType.STRING)
	private Place placeName;

	private Point placePoint;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	public void updatePlaceName(Place placeName) {
		this.placeName = placeName;
	}

	public void updatePlacePoint(Point placePoint) {
		this.placePoint = placePoint;
	}
}
