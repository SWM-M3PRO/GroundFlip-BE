package com.m3pro.groundflip.domain.entity;

import java.time.LocalDateTime;

import org.locationtech.jts.geom.Point;

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
public class Pixel {
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

	@Column(name = "user_id")
	private Long userId;

	private Long communityId;

	private LocalDateTime createdAt;

	private LocalDateTime userOccupiedAt;

	private LocalDateTime communityOccupiedAt;

	public void updateAddress(String address) {
		this.address = address;
	}

	public void updateUserId(Long userId) {
		this.userId = userId;
	}

	public void updateUserOccupiedAtToNow() {
		userOccupiedAt = LocalDateTime.now();
	}

	public void updateCommunityOccupiedAtToNow() {
		communityOccupiedAt = LocalDateTime.now();
	}

	public void updateUserOccupiedAt(LocalDateTime localDateTime) {
		userOccupiedAt = localDateTime;
	}

	public void updateCommunityOccupiedAt(LocalDateTime localDateTime) {
		communityOccupiedAt = localDateTime;
	}
}
