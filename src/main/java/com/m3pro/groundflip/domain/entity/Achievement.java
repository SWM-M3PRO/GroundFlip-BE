package com.m3pro.groundflip.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
public class Achievement {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "achievement_id")
	private Long id;

	private String name;

	private String badgeImageUrl;

	private Integer completionGoal;

	private Long categoryId;

	private String description;

	private LocalDateTime createdAt;

	private String rewardType;

	private Integer rewardAmount;

	private LocalDateTime startAt;

	private LocalDateTime endAt;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "next_achievement_id")
	private Achievement nextAchievement;
}
