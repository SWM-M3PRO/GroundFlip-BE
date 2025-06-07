package com.m3pro.groundflip.scheduler.entity;

import java.util.Objects;

import org.springframework.data.redis.core.ZSetOperations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RankingDetail {
	private Long id;
	private Long currentPixelCount;
	private Long ranking;

	public static RankingDetail from(ZSetOperations.TypedTuple<String> typedTuple, Long ranking) {
		return new RankingDetail(
			Long.parseLong(Objects.requireNonNull(typedTuple.getValue())),
			Objects.requireNonNull(typedTuple.getScore()).longValue(),
			ranking
		);
	}
}
