package com.m3pro.groundflip.domain.dto.ranking;

import java.util.Objects;

import org.springframework.data.redis.core.ZSetOperations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Ranking {
	private Long userId;
	private Long score;

	public static Ranking from(ZSetOperations.TypedTuple<String> typedTuple) {
		return new Ranking(
			Long.parseLong(Objects.requireNonNull(typedTuple.getValue())),
			Objects.requireNonNull(typedTuple.getScore()).longValue()
		);
	}
}
