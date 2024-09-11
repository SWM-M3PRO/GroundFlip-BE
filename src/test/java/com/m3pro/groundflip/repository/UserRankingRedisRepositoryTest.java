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
class UserRankingRedisRepositoryTest {
	private static final String RANKING_KEY = "current_pixel_ranking";
	@Autowired
	UserRankingRedisRepository userRankingRedisRepository;
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
	@DisplayName("[increaseCurrentPixelCount] 기존 점수에 1을 추가한다.")
	void increaseCurrentPixelCountTest() {
		//Given
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		Long userId = 1L;
		userRankingRedisRepository.saveUserInRanking(userId);

		// When
		userRankingRedisRepository.increaseCurrentPixelCount(userId);

		// Then
		Double score = zSetOperations.score(RANKING_KEY, userId.toString());
		assertThat(score).isEqualTo(1);
	}

	@Test
	@DisplayName("[increaseCurrentPixelCount] 등록되지 않은 userId 라도 0으로 초기화후 1을 더한다.")
	void increaseCurrentPixelCountTestUnregistered() {
		//Given
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		Long userId = 1L;

		// When
		userRankingRedisRepository.increaseCurrentPixelCount(userId);

		// Then
		Double score = zSetOperations.score(RANKING_KEY, userId.toString());
		assertThat(score).isEqualTo(1);
	}

	@Test
	@DisplayName("[decreaseCurrentPixelCount] 기존 점수에 1을 뺀다.")
	void decreaseCurrentPixelCountTest() {
		//Given
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		Long userId = 1L;
		userRankingRedisRepository.saveUserInRanking(userId);
		userRankingRedisRepository.increaseCurrentPixelCount(userId);
		userRankingRedisRepository.increaseCurrentPixelCount(userId);

		// When
		userRankingRedisRepository.decreaseCurrentPixelCount(userId);

		// Then
		Double score = zSetOperations.score(RANKING_KEY, userId.toString());
		assertThat(score).isEqualTo(1);
	}

	@Test
	@DisplayName("[decreaseCurrentPixelCount] 기존 점수가 0이라면 0으로 유지된다.")
	void decreaseCurrentPixelCountTestCaseZero() {
		//Given
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		Long userId = 1L;
		userRankingRedisRepository.saveUserInRanking(userId);

		// When
		userRankingRedisRepository.decreaseCurrentPixelCount(userId);

		// Then
		Double score = zSetOperations.score(RANKING_KEY, userId.toString());
		assertThat(score).isEqualTo(0);
	}

	@Test
	@DisplayName("[save] userId 를 넣으면 0으로 초기화 한다.")
	void saveUserInRankingTest() {
		//Given
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		Long userId = 1L;

		// When
		userRankingRedisRepository.saveUserInRanking(userId);

		// Then
		Double score = zSetOperations.score(RANKING_KEY, userId.toString());
		assertThat(score).isEqualTo(0);
	}

	@Test
	@DisplayName("[delete] userId를 레디스에서 지울 수 있다.")
	void deleteUserInRankingTest() {
		//Given
		ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
		Long userId = 1L;

		// When
		userRankingRedisRepository.deleteUserInRanking(userId);

		// Then
		Double score = zSetOperations.score(RANKING_KEY, userId.toString());
		assertThat(score).isEqualTo(null);
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
		List<Ranking> rankings = userRankingRedisRepository.getRankingsWithCurrentPixelCount();

		//Then
		assertThat(rankings).hasSize(3);
		assertThat(rankings.get(0).getId()).isEqualTo(userId3);
		assertThat(rankings.get(1).getId()).isEqualTo(userId1);
		assertThat(rankings.get(2).getId()).isEqualTo(userId2);
		assertThat(rankings.get(0).getRank()).isEqualTo(1);
		assertThat(rankings.get(1).getRank()).isEqualTo(2);
		assertThat(rankings.get(2).getRank()).isEqualTo(3);
	}

	@Test
	@DisplayName("[getUserRank] userId 에 해당하는 점수를 반환한다.")
	void getUserCurrentPixelRank() {
		//Given
		Long userId1 = 1L;
		Long userId2 = 2L;
		Long userId3 = 3L;
		setRanking(userId1, userId2, userId3);

		// When
		Optional<Long> score = userRankingRedisRepository.getUserCurrentPixelRank(userId1);

		//Then
		assertThat(score.isPresent()).isEqualTo(true);
		//noinspection OptionalGetWithoutIsPresent
		assertThat(score.get()).isEqualTo(2);
	}

	@Test
	@DisplayName("[getUserRank] 없는 userId를 넣으면 Optional에 값이 없다.")
	void getUserCurrentPixelRankTestNullPointException() {
		//Given
		Long userId1 = 1L;

		// When
		Optional<Long> score = userRankingRedisRepository.getUserCurrentPixelRank(userId1);

		// Then
		assertThat(score.isEmpty()).isEqualTo(true);
	}

	@Test
	@DisplayName("[getUserRank] userId 에 해당하는 점수를 반환한다.")
	void getUserCurrentPixelCountTest() {
		//Given
		Long userId1 = 1L;
		Long userId2 = 2L;
		Long userId3 = 3L;
		setRanking(userId1, userId2, userId3);

		// When
		Optional<Long> currentPixelCount = userRankingRedisRepository.getUserCurrentPixelCount(userId1);

		//Then
		assertThat(currentPixelCount.isPresent()).isEqualTo(true);
		//noinspection OptionalGetWithoutIsPresent
		assertThat(currentPixelCount.get()).isEqualTo(2);
	}

	@Test
	@DisplayName("[getUserCurrentPixelCount] 없는 userId를 넣으면 Optional에 값이 없다.")
	void getUserCurrentPixelCountTestNull() {
		//Given
		Long userId1 = 1L;

		// When
		Optional<Long> currentPixelCount = userRankingRedisRepository.getUserCurrentPixelCount(userId1);

		// Then
		assertThat(currentPixelCount.isEmpty()).isEqualTo(true);
	}

	private void setRanking(Long userId1, Long userId2, Long userId3) {
		userRankingRedisRepository.saveUserInRanking(userId1);
		userRankingRedisRepository.saveUserInRanking(userId2);
		userRankingRedisRepository.saveUserInRanking(userId3);

		userRankingRedisRepository.increaseCurrentPixelCount(userId1);
		userRankingRedisRepository.increaseCurrentPixelCount(userId1);
		userRankingRedisRepository.increaseCurrentPixelCount(userId2);
		userRankingRedisRepository.increaseCurrentPixelCount(userId3);
		userRankingRedisRepository.increaseCurrentPixelCount(userId3);
		userRankingRedisRepository.increaseCurrentPixelCount(userId3);
	}
}