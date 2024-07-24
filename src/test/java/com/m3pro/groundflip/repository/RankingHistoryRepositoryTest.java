package com.m3pro.groundflip.repository;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.m3pro.groundflip.domain.dto.ranking.UserRankingResponse;
import com.m3pro.groundflip.domain.entity.RankingHistory;
import com.m3pro.groundflip.domain.entity.User;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RankingHistoryRepositoryTest {
	@Autowired
	private RankingHistoryRepository rankingHistoryRepository;
	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		rankingHistoryRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	@Transactional
	@DisplayName("[findAllByYearAndWeek] 과거의 랭킹이 정상적으로 조회되는 지 확인")
	void findAllByYearAndWeekTest() {
		User savedUser1 = userRepository.save(User.builder()
			.email("test1@naver.com")
			.build());
		User savedUser2 = userRepository.save(User.builder()
			.email("test2@naver.com")
			.build());
		User savedUser3 = userRepository.save(User.builder()
			.email("test3@naver.com")
			.build());

		rankingHistoryRepository.save(RankingHistory.builder()
			.userId(savedUser1.getId())
			.currentPixelCount(30L)
			.ranking(3L)
			.year(2024)
			.week(29)
			.build());
		rankingHistoryRepository.save(RankingHistory.builder()
			.userId(savedUser2.getId())
			.currentPixelCount(40L)
			.ranking(2L)
			.year(2024)
			.week(29)
			.build());
		rankingHistoryRepository.save(RankingHistory.builder()
			.userId(savedUser3.getId())
			.currentPixelCount(50L)
			.ranking(1L)
			.year(2024)
			.week(29)
			.build());

		List<UserRankingResponse> result = rankingHistoryRepository.findAllByYearAndWeek(2024, 29);

		Assertions.assertThat(result.size()).isEqualTo(3);
		Assertions.assertThat(result.get(0).getUserId()).isEqualTo(savedUser3.getId());
		Assertions.assertThat(result.get(1).getUserId()).isEqualTo(savedUser2.getId());
		Assertions.assertThat(result.get(2).getUserId()).isEqualTo(savedUser1.getId());
	}
}
