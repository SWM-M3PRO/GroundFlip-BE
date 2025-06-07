package com.m3pro.groundflip.scheduler.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SchedulerCommunityRankingRedisRepository extends SchedulerRankingRedisRepository {
	public SchedulerCommunityRankingRedisRepository(RedisTemplate<String, String> redisTemplate) {
		super(redisTemplate, "group_current_pixel_ranking");
	}
}
