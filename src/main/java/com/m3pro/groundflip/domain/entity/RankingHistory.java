package com.m3pro.groundflip.domain.entity;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import com.m3pro.groundflip.domain.entity.global.BaseTimeEntity;
import com.m3pro.groundflip.scheduler.entity.RankingDetail;
import com.m3pro.groundflip.util.DateUtils;

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

	public static RankingHistory of(RankingDetail rankingDetail) {
		LocalDateTime now = LocalDateTime.now();

		int year = now.getYear();
		int week = DateUtils.getWeekOfDate(now.toLocalDate());

		if (now.getDayOfWeek() == DayOfWeek.MONDAY && now.getHour() == 0) {
			week -= 1;
		}

		return RankingHistory.builder()
			.userId(rankingDetail.getId())
			.ranking(rankingDetail.getRanking())
			.currentPixelCount(rankingDetail.getCurrentPixelCount())
			.year(year)
			.week(week)
			.build();
	}

	public void update(Long currentPixelCount, Long ranking) {
		this.currentPixelCount = currentPixelCount;
		this.ranking = ranking;
	}
}
