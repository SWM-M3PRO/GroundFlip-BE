package com.m3pro.groundflip.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import com.m3pro.groundflip.domain.dto.ranking.Ranking;

public class RankingRedisRepository {
	private static final int RANKING_START_INDEX = 0;
	private static final int RANKING_END_INDEX = 29;
	private final String currentPixelRankingKey;
	private final String accumulatePixelRankingKey;
	private final ZSetOperations<String, String> zSetOperations;

	public RankingRedisRepository(RedisTemplate<String, String> redisTemplate, String currentPixelRankingKey,
		String accumulatePixelRankingKey) {
		this.currentPixelRankingKey = currentPixelRankingKey;
		this.accumulatePixelRankingKey = accumulatePixelRankingKey;
		zSetOperations = redisTemplate.opsForZSet();
	}

	public void increaseCurrentPixelCount(Long userId) {
		zSetOperations.incrementScore(currentPixelRankingKey, userId.toString(), 1);
	}

	public void increaseAccumulatePixelCount(Long userId) {
		zSetOperations.incrementScore(accumulatePixelRankingKey, userId.toString(), 1);
	}

	public void decreaseCurrentPixelCount(Long userId) {
		Double currentScore = zSetOperations.score(currentPixelRankingKey, userId.toString());
		if (currentScore != null && currentScore > 0) {
			zSetOperations.incrementScore(currentPixelRankingKey, userId.toString(), -1);
		}
	}

	public void saveUserInRanking(Long userId) {
		Double currentPixelScore = zSetOperations.score(currentPixelRankingKey, userId.toString());
		Double accumulatePixelScore = zSetOperations.score(accumulatePixelRankingKey, userId.toString());

		if (currentPixelScore == null) {
			zSetOperations.add(currentPixelRankingKey, userId.toString(), 0);
		}
		if (accumulatePixelScore == null) {
			zSetOperations.add(accumulatePixelRankingKey, userId.toString(), 0);
		}
	}

	public void deleteUserInRanking(Long userId) {
		Double currentPixelScore = zSetOperations.score(currentPixelRankingKey, userId.toString());
		Double accumulatePixelScore = zSetOperations.score(accumulatePixelRankingKey, userId.toString());

		if (currentPixelScore != null) {
			zSetOperations.remove(currentPixelRankingKey, userId.toString());
		}
		if (accumulatePixelScore == null) {
			zSetOperations.remove(accumulatePixelRankingKey, userId.toString());
		}
	}

	public List<Ranking> getRankingsWithCurrentPixelCount() {
		Set<ZSetOperations.TypedTuple<String>> typedTuples = zSetOperations.reverseRangeWithScores(
			currentPixelRankingKey,
			RANKING_START_INDEX, RANKING_END_INDEX);
		if (typedTuples == null) {
			return new ArrayList<>();
		}

		List<Ranking> rankings = new ArrayList<>();
		long rank = 1;
		for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
			rankings.add(Ranking.from(typedTuple, rank++));
		}
		return rankings;
	}

	public Optional<Long> getCurrentPixelRank(Long userId) {
		Long rank = zSetOperations.reverseRank(currentPixelRankingKey, userId.toString());
		return Optional.ofNullable(rank).map(r -> r + 1);
	}

	public Optional<Long> getCurrentPixelCount(Long userId) {
		Double currentPixelCount = zSetOperations.score(currentPixelRankingKey, userId.toString());
		return Optional.ofNullable(currentPixelCount).map(Double::longValue);
	}

	public Optional<Long> getAccumulatePixelCount(Long userId) {
		Double accumulatePixelCount = zSetOperations.score(accumulatePixelRankingKey, userId.toString());
		return Optional.ofNullable(accumulatePixelCount).map(Double::longValue);
	}
}
