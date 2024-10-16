package com.m3pro.groundflip.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CommunityRankingRedisRepository extends RankingRedisRepository {
	public CommunityRankingRedisRepository(RedisTemplate<String, String> redisTemplate) {
		super(redisTemplate, "group_current_pixel_ranking", "group_accumulate_pixel_ranking");
	}

	public void addRanking(String groupId, double score) {
		zSetOperations.add("group_current_pixel_ranking", groupId, score);

		zSetOperations.add("group_accumulate_pixel_ranking", groupId, score);
	}
}
