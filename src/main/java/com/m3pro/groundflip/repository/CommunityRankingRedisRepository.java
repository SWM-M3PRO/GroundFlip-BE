package com.m3pro.groundflip.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CommunityRankingRedisRepository {
	private static final String CURRENT_PIXEL_RANKING_KEY = "group_current_pixel_ranking";
	private static final String ACCUMULATE_PIXEL_RANKING_KEY = "group_accumulate_pixel_ranking";
	private static final int RANKING_START_INDEX = 0;
	private static final int RANKING_END_INDEX = 29;
	private final RedisTemplate<String, String> redisTemplate;
	private ZSetOperations<String, String> zSetOperations;

	@PostConstruct
	void init() {
		zSetOperations = redisTemplate.opsForZSet();
	}

	public void increaseCurrentPixelCount(Long communityId) {
		zSetOperations.incrementScore(CURRENT_PIXEL_RANKING_KEY, communityId.toString(), 1);
	}

	public void decreaseCurrentPixelCount(Long communityId) {
		Double currentScore = zSetOperations.score(CURRENT_PIXEL_RANKING_KEY, communityId.toString());
		if (currentScore != null && currentScore > 0) {
			zSetOperations.incrementScore(CURRENT_PIXEL_RANKING_KEY, communityId.toString(), -1);
		}
	}

	public void increaseAccumulatePixelCount(Long communityId) {
		zSetOperations.incrementScore(ACCUMULATE_PIXEL_RANKING_KEY, communityId.toString(), 1);
		System.out.println("값 증가 +++++++++++++++++++++++");
	}
}
