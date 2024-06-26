package com.m3pro.groundflip.domain.entity;

import org.locationtech.jts.geom.Point;

import com.m3pro.groundflip.domain.entity.global.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Pixel extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pixel_id")
	private Long id;

	private Long x;

	private Long y;

	@Column(columnDefinition = "POINT SRID 4326 NOT NULL")
	private Point coordinate;

	private String address;

	private Integer addressNumber;
}
