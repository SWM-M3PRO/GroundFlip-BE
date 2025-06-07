package com.m3pro.groundflip.scheduler.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SchedulerUserRankingRedisRepository extends SchedulerRankingRedisRepository {
	public SchedulerUserRankingRedisRepository(RedisTemplate<String, String> redisTemplate) {
		super(redisTemplate, "current_pixel_ranking");
	}
}
