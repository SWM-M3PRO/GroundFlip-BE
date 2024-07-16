package com.m3pro.groundflip.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.ActiveProfiles;

import com.m3pro.groundflip.domain.dto.ranking.Ranking;

@SpringBootTest
@ActiveProfiles("test")
class RankingRedisRepositoryTest {
	private static final String RANKING_KEY = "current_pixel_ranking";
	@Autowired
	RankingRedisRepository rankingRedisRepository;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@AfterEach
	public void tearDown() {
		redisTemplate.execute((RedisCallback<Void>)connection -> {
			connection.serverCommands().flushDb();
			return null;
		});
	}

	@Test
	@DisplayName("[increaseScore] 기존 점수에 1을 추가한다.")
	void increaseCurrentPixelCountTest() {
		//Given
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		Long userId = 1L;
		rankingRedisRepository.save(userId);

		// When
		rankingRedisRepository.increaseCurrentPixelCount(userId);

		// Then
		Double score = zSetOperations.score(RANKING_KEY, userId.toString());
		assertThat(score).isEqualTo(1);
	}

	@Test
	@DisplayName("[increaseScore] 등록되지 않은 userId 라도 0으로 초기화후 1을 더한다.")
	void increaseCurrentPixelCountTestUnregistered() {
		//Given
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		Long userId = 1L;

		// When
		rankingRedisRepository.increaseCurrentPixelCount(userId);

		// Then
		Double score = zSetOperations.score(RANKING_KEY, userId.toString());
		assertThat(score).isEqualTo(1);
	}

	@Test
	@DisplayName("[decreaseScore] 기존 점수에 1을 뺀다.")
	void decreaseCurrentPixelCountTest() {
		//Given
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		Long userId = 1L;
		rankingRedisRepository.save(userId);
		rankingRedisRepository.increaseCurrentPixelCount(userId);
		rankingRedisRepository.increaseCurrentPixelCount(userId);

		// When
		rankingRedisRepository.decreaseCurrentPixelCount(userId);

		// Then
		Double score = zSetOperations.score(RANKING_KEY, userId.toString());
		assertThat(score).isEqualTo(1);
	}

	@Test
	@DisplayName("[decreaseScore] 기존 점수가 0이라면 0으로 유지된다.")
	void decreaseCurrentPixelCountTestCaseZero() {
		//Given
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		Long userId = 1L;
		rankingRedisRepository.save(userId);

		// When
		rankingRedisRepository.decreaseCurrentPixelCount(userId);

		// Then
		Double score = zSetOperations.score(RANKING_KEY, userId.toString());
		assertThat(score).isEqualTo(0);
	}

	@Test
	@DisplayName("[save] userId 를 넣으면 0으로 초기화 한다.")
	void saveTest() {
		//Given
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		Long userId = 1L;

		// When
		rankingRedisRepository.save(userId);

		// Then
		Double score = zSetOperations.score(RANKING_KEY, userId.toString());
		assertThat(score).isEqualTo(0);
	}

	@Test
	@DisplayName("[getRankingsWithScore] 점수 순으로 내림차순으로 30개가 리스트에 반환된다. 30개가 되지 않는다면 채워진 개수만 반환한다.")
	void getRankingsWithScoreTest() {
		//Given
		Long userId1 = 1L;
		Long userId2 = 2L;
		Long userId3 = 3L;
		setRanking(userId1, userId2, userId3);

		// When
		List<Ranking> rankings = rankingRedisRepository.getRankingsWithCurrentPixelCount();

		//Then
		assertThat(rankings).hasSize(3);
		assertThat(rankings.get(0).getUserId()).isEqualTo(userId3);
		assertThat(rankings.get(1).getUserId()).isEqualTo(userId1);
		assertThat(rankings.get(2).getUserId()).isEqualTo(userId2);
	}

	@Test
	@DisplayName("[getUserRank] userId 에 해당하는 점수를 반환한다.")
	void getUserRank() {
		//Given
		Long userId1 = 1L;
		Long userId2 = 2L;
		Long userId3 = 3L;
		setRanking(userId1, userId2, userId3);

		// When
		Optional<Long> score = rankingRedisRepository.getUserRank(userId1);

		//Then
		assertThat(score.isPresent()).isEqualTo(true);
		assertThat(score.get()).isEqualTo(2);
	}

	@Test
	@DisplayName("[getUserRank] 없는 userId를 넣으면 NullPointException을 반환한다.")
	void getUserRankTestNullPointException() {
		//Given
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		Long userId1 = 1L;

		// When
		Optional<Long> score = rankingRedisRepository.getUserRank(userId1);

		// Then
		assertThat(score.isEmpty()).isEqualTo(true);
	}

	private void setRanking(Long userId1, Long userId2, Long userId3) {
		rankingRedisRepository.save(userId1);
		rankingRedisRepository.save(userId2);
		rankingRedisRepository.save(userId3);

		rankingRedisRepository.increaseCurrentPixelCount(userId1);
		rankingRedisRepository.increaseCurrentPixelCount(userId1);
		rankingRedisRepository.increaseCurrentPixelCount(userId2);
		rankingRedisRepository.increaseCurrentPixelCount(userId3);
		rankingRedisRepository.increaseCurrentPixelCount(userId3);
		rankingRedisRepository.increaseCurrentPixelCount(userId3);
	}
}