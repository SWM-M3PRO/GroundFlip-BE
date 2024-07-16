package com.m3pro.groundflip.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RankingRedisRepository {
	private static final String RANKING_KEY = "current_pixel_ranking";
	private final RedisTemplate<String, String> redisTemplate;

	public void increaseScore(Long userId) {
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		zSetOperations.incrementScore(RANKING_KEY, userId.toString(), 1);
	}

	public void decreaseScore(Long userId) {
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		zSetOperations.incrementScore(RANKING_KEY, userId.toString(), -1);
	}
}
