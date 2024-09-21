package com.m3pro.groundflip.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import com.m3pro.groundflip.domain.dto.ranking.Ranking;
import com.m3pro.groundflip.domain.entity.User;

@Repository
public class UserRankingRedisRepository extends RankingRedisRepository {
	public UserRankingRedisRepository(RedisTemplate<String, String> redisTemplate) {
		super(redisTemplate, "current_pixel_ranking", "accumulate_pixel_ranking");
	}

	public Map<Long, Long> getCurrentPixelCountByUser(List<User> users) {
		long startTime = System.nanoTime();
		Map<Long, Long> userScore = new HashMap<>();
		List<Object> scores = redisTemplate.executePipelined((RedisCallback<Object>)connection -> {
			users.forEach((user) -> {
				connection.zScore(currentPixelRankingKey.getBytes(), user.getId().toString().getBytes());
			});
			return null;
		});
		for (int i = 0; i < users.size(); i++) {
			Long userId = users.get(i).getId();
			Double score = (Double)scores.get(i);
			userScore.put(userId, score.longValue());
		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime) / 1_000_000;
		System.out.println("레디스 각각" + " 실행 시간: " + duration + " ms");
		return userScore;
	}

	public List<Ranking> getCurrentPixelCountByUser2() {
		long startTime = System.nanoTime();
		Set<ZSetOperations.TypedTuple<String>> typedTuples = zSetOperations.reverseRangeWithScores(
			currentPixelRankingKey, 0, -1);
		if (typedTuples == null) {
			return new ArrayList<>();
		}

		List<Ranking> rankings = new ArrayList<>();
		long rank = 1;
		for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
			rankings.add(Ranking.from(typedTuple, rank++));
		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime) / 1_000_000;
		System.out.println("레디스 한번에" + " 실행 시간: " + duration + " ms");
		return rankings;
	}
}
