package com.m3pro.groundflip.domain.entity;

import java.time.LocalDateTime;

import com.m3pro.groundflip.domain.entity.global.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "community")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Community extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "community_id")
	private Long id;

	private String name;

	private String pixelColor;

	private String profileImageUrl;

	private String backgroundImageUrl;

	private String description;

	private LocalDateTime deletedAt;

}
