package com.m3pro.groundflip.domain.entity;

import java.time.LocalDateTime;

import com.m3pro.groundflip.domain.entity.global.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class UserAchievement extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "achievement_id")
	private Achievement achievement;

	@Column(name = "current_value")
	private Integer currentValue;

	@Column(name = "obtained_at")
	private LocalDateTime obtainedAt;

	@Column(name = "is_reward_received")
	private Boolean isRewardReceived;

	public void increaseCurrentValue() {
		this.currentValue++;
	}

	public void setObtainedAt() {
		this.obtainedAt = LocalDateTime.now();
	}
}
