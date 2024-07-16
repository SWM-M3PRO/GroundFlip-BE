package com.m3pro.groundflip.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import com.m3pro.groundflip.domain.dto.ranking.Ranking;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RankingRedisRepository {
	private static final String RANKING_KEY = "current_pixel_ranking";
	private static final int RANKING_START_INDEX = 0;
	private static final int RANKING_END_INDEX = 29;
	private final RedisTemplate<String, String> redisTemplate;

	public void increaseCurrentPixelCount(Long userId) {
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		zSetOperations.incrementScore(RANKING_KEY, userId.toString(), 1);
	}

	public void decreaseCurrentPixelCount(Long userId) {
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		Double currentScore = zSetOperations.score(RANKING_KEY, userId.toString());
		if (currentScore != null && currentScore > 0) {
			zSetOperations.incrementScore(RANKING_KEY, userId.toString(), -1);
		}
	}

	public void save(Long userId) {
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		zSetOperations.add(RANKING_KEY, userId.toString(), 0);
	}

	public List<Ranking> getRankingsWithCurrentPixelCount() {
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		return new ArrayList<>(
			(Objects.requireNonNull(
				zSetOperations.reverseRangeWithScores(RANKING_KEY, RANKING_START_INDEX, RANKING_END_INDEX))))
			.stream().map(Ranking::from).toList();
	}

	public Optional<Long> getUserRank(Long userId) {
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		Long rank = zSetOperations.reverseRank(RANKING_KEY, userId.toString());
		return Optional.ofNullable(rank).map(r -> r + 1);
	}

	public Optional<Long> getUserCurrentPixelCount(Long userId) {
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		Double currentPixelCount = zSetOperations.score(RANKING_KEY, userId.toString());
		return Optional.ofNullable(currentPixelCount).map(Double::longValue);
	}
}
