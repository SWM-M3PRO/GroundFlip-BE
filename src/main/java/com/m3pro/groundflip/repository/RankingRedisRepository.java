package com.m3pro.groundflip.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import com.m3pro.groundflip.domain.dto.ranking.Ranking;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RankingRedisRepository {
	private static final String RANKING_KEY = "current_pixel_ranking";
	private static final int RANKING_START_INDEX = 0;
	private static final int RANKING_END_INDEX = 29;
	private final RedisTemplate<String, String> redisTemplate;
	private ZSetOperations<String, String> zSetOperations;

	@PostConstruct
	void init() {
		zSetOperations = redisTemplate.opsForZSet();
	}

	public void increaseCurrentPixelCount(Long userId) {
		zSetOperations.incrementScore(RANKING_KEY, userId.toString(), 1);
	}

	public void decreaseCurrentPixelCount(Long userId) {
		Double currentScore = zSetOperations.score(RANKING_KEY, userId.toString());
		if (currentScore != null && currentScore > 0) {
			zSetOperations.incrementScore(RANKING_KEY, userId.toString(), -1);
		}
	}

	public void saveUserInRedis(Long userId) {
		Double score = zSetOperations.score(RANKING_KEY, userId.toString());

		if (score == null) {
			zSetOperations.add(RANKING_KEY, userId.toString(), 0);
		}
	}

	public List<Ranking> getRankingsWithCurrentPixelCount() {
		Set<ZSetOperations.TypedTuple<String>> typedTuples = zSetOperations.reverseRangeWithScores(RANKING_KEY,
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

	public Optional<Long> getUserRank(Long userId) {
		Long rank = zSetOperations.reverseRank(RANKING_KEY, userId.toString());
		return Optional.ofNullable(rank).map(r -> r + 1);
	}

	public Optional<Long> getUserCurrentPixelCount(Long userId) {
		Double currentPixelCount = zSetOperations.score(RANKING_KEY, userId.toString());
		return Optional.ofNullable(currentPixelCount).map(Double::longValue);
	}
}
