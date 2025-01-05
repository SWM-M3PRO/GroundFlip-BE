package com.m3pro.groundflip.domain.entity;

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
public class RankingHistory extends BaseTimeEntity {
	@Id
	@Column(name = "ranking_history_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	Long userId;

	Long ranking;

	Long currentPixelCount;

	Integer year;

	Integer week;
}
