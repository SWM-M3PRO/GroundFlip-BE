package com.m3pro.groundflip.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRankingRedisRepository extends RankingRedisRepository {
	public UserRankingRedisRepository(RedisTemplate<String, String> redisTemplate) {
		super(redisTemplate, "current_pixel_ranking", "accumulate_pixel_ranking");
	}
}
