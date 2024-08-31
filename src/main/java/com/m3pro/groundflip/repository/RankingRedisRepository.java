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
	private static final String CURRENT_PIXEL_RANKING_KEY = "current_pixel_ranking";
	private static final String ACCUMULATE_PIXEL_RANKING_KEY = "accumulate_pixel_ranking";
	private static final int RANKING_START_INDEX = 0;
	private static final int RANKING_END_INDEX = 29;
	private final RedisTemplate<String, String> redisTemplate;
	private ZSetOperations<String, String> zSetOperations;

	@PostConstruct
	void init() {
		zSetOperations = redisTemplate.opsForZSet();
	}

	public void increaseCurrentPixelCount(Long userId) {
		zSetOperations.incrementScore(CURRENT_PIXEL_RANKING_KEY, userId.toString(), 1);
	}

	public void increaseAccumulatePixelCount(Long userId) {
		zSetOperations.incrementScore(ACCUMULATE_PIXEL_RANKING_KEY, userId.toString(), 1);
	}

	public void decreaseCurrentPixelCount(Long userId) {
		Double currentScore = zSetOperations.score(CURRENT_PIXEL_RANKING_KEY, userId.toString());
		if (currentScore != null && currentScore > 0) {
			zSetOperations.incrementScore(CURRENT_PIXEL_RANKING_KEY, userId.toString(), -1);
		}
	}

	public void saveUserInRanking(Long userId) {
		Double currentPixelScore = zSetOperations.score(CURRENT_PIXEL_RANKING_KEY, userId.toString());
		Double accumulatePixelScore = zSetOperations.score(ACCUMULATE_PIXEL_RANKING_KEY, userId.toString());

		if (currentPixelScore == null) {
			zSetOperations.add(CURRENT_PIXEL_RANKING_KEY, userId.toString(), 0);
		}
		if (accumulatePixelScore == null) {
			zSetOperations.add(ACCUMULATE_PIXEL_RANKING_KEY, userId.toString(), 0);
		}
	}

	public void deleteUserInRanking(Long userId) {
		Double currentPixelScore = zSetOperations.score(CURRENT_PIXEL_RANKING_KEY, userId.toString());
		Double accumulatePixelScore = zSetOperations.score(ACCUMULATE_PIXEL_RANKING_KEY, userId.toString());

		if (currentPixelScore != null) {
			zSetOperations.remove(CURRENT_PIXEL_RANKING_KEY, userId.toString());
		}
		if (accumulatePixelScore == null) {
			zSetOperations.remove(ACCUMULATE_PIXEL_RANKING_KEY, userId.toString());
		}
	}

	public List<Ranking> getRankingsWithCurrentPixelCount() {
		Set<ZSetOperations.TypedTuple<String>> typedTuples = zSetOperations.reverseRangeWithScores(
			CURRENT_PIXEL_RANKING_KEY,
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
		Long rank = zSetOperations.reverseRank(CURRENT_PIXEL_RANKING_KEY, userId.toString());
		return Optional.ofNullable(rank).map(r -> r + 1);
	}

	public Optional<Long> getUserCurrentPixelCount(Long userId) {
		Double currentPixelCount = zSetOperations.score(CURRENT_PIXEL_RANKING_KEY, userId.toString());
		return Optional.ofNullable(currentPixelCount).map(Double::longValue);
	}

	public Optional<Long> getUserAccumulatePixelCount(Long userId) {
		Double accumulatePixelCount = zSetOperations.score(ACCUMULATE_PIXEL_RANKING_KEY, userId.toString());
		return Optional.ofNullable(accumulatePixelCount).map(Double::longValue);
	}
}
