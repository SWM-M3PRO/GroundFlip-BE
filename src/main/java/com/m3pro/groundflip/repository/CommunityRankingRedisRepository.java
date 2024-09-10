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

	public Optional<Long> getCommunityCurrentPixelRank(Long communityId) {
		Long rank = zSetOperations.reverseRank(CURRENT_PIXEL_RANKING_KEY, communityId.toString());
		return Optional.ofNullable(rank).map(r -> r + 1);
	}

	public Optional<Long> getCommunityCurrentPixelCount(Long communityId) {
		Double currentPixelCount = zSetOperations.score(CURRENT_PIXEL_RANKING_KEY, communityId.toString());
		return Optional.ofNullable(currentPixelCount).map(Double::longValue);
	}

	public Optional<Long> getCommunityAccumulatePixelCount(Long communityId) {
		Double accumulatePixelCount = zSetOperations.score(ACCUMULATE_PIXEL_RANKING_KEY, communityId.toString());
		return Optional.ofNullable(accumulatePixelCount).map(Double::longValue);
	}
}
