package com.m3pro.groundflip.scheduler.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import com.m3pro.groundflip.scheduler.entity.RankingDetail;

public class SchedulerRankingRedisRepository {
	private static final int RANKING_START_INDEX = 0;
	private static final int RANKING_END_INDEX = -1;
	private final String currentPixelRankingKey;
	private final ZSetOperations<String, String> zSetOperations;

	public SchedulerRankingRedisRepository(RedisTemplate<String, String> redisTemplate, String currentPixelRankingKey) {
		this.currentPixelRankingKey = currentPixelRankingKey;
		zSetOperations = redisTemplate.opsForZSet();
	}

	public List<RankingDetail> getRankingsWithCurrentPixelCount() {
		Set<ZSetOperations.TypedTuple<String>> typedTuples = zSetOperations.reverseRangeWithScores(
			currentPixelRankingKey,
			RANKING_START_INDEX, RANKING_END_INDEX);
		if (typedTuples == null) {
			return new ArrayList<>();
		}

		List<RankingDetail> rankingDetails = new ArrayList<>();
		long ranking = 1;
		for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
			rankingDetails.add(RankingDetail.from(typedTuple, ranking++));
		}
		return rankingDetails;
	}

	public void resetAllScoresToZero() {
		Set<String> ids = zSetOperations.range(currentPixelRankingKey, RANKING_START_INDEX, -1); // Get all members
		if (ids != null) {
			for (String id : ids) {
				zSetOperations.add(currentPixelRankingKey, id, 0); // Update the score to 0
			}
		}
	}
}
