package com.m3pro.groundflip.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.ranking.Ranking;
import com.m3pro.groundflip.domain.dto.ranking.UserRankingResponse;
import com.m3pro.groundflip.domain.entity.RankingHistory;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.RankingHistoryRepository;
import com.m3pro.groundflip.repository.RankingRedisRepository;
import com.m3pro.groundflip.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {
	@Mock
	private RankingRedisRepository rankingRedisRepository;
	@Mock
	private RankingHistoryRepository rankingHistoryRepository;
	@Mock
	private UserRepository userRepository;
	@InjectMocks
	private RankingService rankingService;

	@BeforeEach
	void init() {
		reset(rankingRedisRepository);
	}

	@Test
	@DisplayName("[increaseCurrentPixelCount] userId 에 해당하는 현재 소유 픽셀의 개수를 1 증가시킨다.")
	void increaseCurrentPixelCountTest() {
		Long userId = 1L;

		rankingService.increaseCurrentPixelCount(userId);

		verify(rankingRedisRepository, times(1)).increaseCurrentPixelCount(userId);
	}

	@Test
	@DisplayName("[decreasePixelCount] userId 에 해당하는 현재 소유 픽셀의 개수를 1 감소시킨다.")
	void decreasePixelCountTest() {
		Long userId = 1L;

		rankingService.decreaseCurrentPixelCount(userId);

		verify(rankingRedisRepository, times(1)).decreaseCurrentPixelCount(userId);
	}

	@Test
	@DisplayName("[decreasePixelCount] occupyingUserId 에 해당하는 현재 소유 픽셀의 개수를 1 증가시키고 deprivedUserId 에 해당하는 현재 소유 픽셀의 개수를 1 감소시킨다.")
	void updateRankingAfterOccupyTest() {
		Long occupyingUserId = 1L;
		Long deprivedUserId = 2L;

		rankingService.updateRankingAfterOccupy(occupyingUserId, deprivedUserId);

		verify(rankingRedisRepository, times(1)).decreaseCurrentPixelCount(deprivedUserId);
		verify(rankingRedisRepository, times(1)).increaseCurrentPixelCount(occupyingUserId);
	}

	@Test
	@DisplayName("[getCurrentPixelCount] userId 가 소유한 픽셀의 개수를 반환한다.")
	void getCurrentPixelCountFromCacheTest() {
		Long userId = 1L;
		when(rankingRedisRepository.getUserCurrentPixelCount(any())).thenReturn(Optional.of(15L));

		Long count = rankingService.getCurrentPixelCountFromCache(userId);

		assertThat(count).isEqualTo(15L);
	}

	@Test
	@DisplayName("[getCurrentPixelCount] userId가 sortedSet에 없다면 0 반환")
	void getCurrentPixelCountFromCacheTestNull() {
		Long userId = 1L;
		when(rankingRedisRepository.getUserCurrentPixelCount(any())).thenReturn(Optional.empty());

		Long count = rankingService.getCurrentPixelCountFromCache(userId);

		assertThat(count).isEqualTo(0L);
	}

	@Test
	@DisplayName("[getUserRankInfo] 이번주에 대해 user가 없다면 user not found")
	void getUserRankInfoTestUserNotFoundExceptionFromCache() {
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		AppException exception = assertThrows(AppException.class,
			() -> rankingService.getUserRankInfo(userId, LocalDate.now()));

		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("[getUserRankInfo] userId에 해당하는 순위 정보 반환")
	void getUserRankFromCacheInfoTest() {
		Long userId = 1L;
		User user = User.builder()
			.id(userId)
			.nickname("test")
			.profileImage("test")
			.build();
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(rankingRedisRepository.getUserCurrentPixelCount(any())).thenReturn(Optional.of(15L));
		when(rankingRedisRepository.getUserRank(any())).thenReturn(Optional.of(1L));

		UserRankingResponse userRankingResponse = rankingService.getUserRankInfo(userId, LocalDate.now());

		assertThat(userRankingResponse.getUserId()).isEqualTo(userId);
		assertThat(userRankingResponse.getCurrentPixelCount()).isEqualTo(15L);
		assertThat(userRankingResponse.getNickname()).isEqualTo("test");
		assertThat(userRankingResponse.getRank()).isEqualTo(1L);
		assertThat(userRankingResponse.getProfileImageUrl()).isEqualTo("test");
	}

	@Test
	@DisplayName("[getUserRankFromCache] 사용자가 Redis에 존재하지 않는다면 500 에러를 발생시킨다.")
	void getUserRankFromCacheUserNotFoundInRedis() {
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder()
			.id(1L)
			.build()));

		when(rankingRedisRepository.getUserRank(userId)).thenReturn(Optional.empty());

		AppException exception = assertThrows(AppException.class,
			() -> rankingService.getUserRankInfo(userId, LocalDate.now()));

		assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
	}

	@Test
	@DisplayName("[getAllUserRanking] 현재 상위 30명의 랭킹을 가져온다.")
	void getAllCurrentWeekRankingTest() {
		List<Ranking> rankings = Arrays.asList(
			new Ranking(1L, 10L, 3L),
			new Ranking(2L, 20L, 1L),
			new Ranking(3L, 15L, 2L)
		);

		List<User> users = Arrays.asList(
			User.builder().id(1L).nickname("User1").profileImage("url1").build(),
			User.builder().id(2L).nickname("User2").profileImage("url2").build(),
			User.builder().id(3L).nickname("User3").profileImage("url3").build()
		);

		when(rankingRedisRepository.getRankingsWithCurrentPixelCount()).thenReturn(rankings);
		when(userRepository.findAllById(anySet())).thenReturn(users);

		List<UserRankingResponse> responses = rankingService.getAllUserRankings(LocalDate.now());

		assertEquals(3, responses.size());
		assertEquals(1L, responses.get(0).getUserId());
		assertEquals(2L, responses.get(1).getUserId());
		assertEquals(3L, responses.get(2).getUserId());
	}

	@Test
	@DisplayName("[getAllUserRanking] 레디스에서 찾은 유저가 DB 에서 찾아온 유저에 필터링된다.")
	void getAllUserRankingTest_UserNotFound() {
		List<Ranking> rankings = Arrays.asList(
			new Ranking(1L, 10L, 3L),
			new Ranking(2L, 20L, 1L),
			new Ranking(3L, 15L, 2L)
		);

		List<User> users = Arrays.asList(
			User.builder().id(1L).nickname("User1").profileImage("url1").build(),
			User.builder().id(2L).nickname("User2").profileImage("url2").build(),
			User.builder().id(3L).nickname("User3").profileImage("url3").build()
		);
		when(rankingRedisRepository.getRankingsWithCurrentPixelCount()).thenReturn(rankings);
		when(userRepository.findAllById(anySet())).thenReturn(
			users.stream().filter(user -> user.getId() != 2L).collect(Collectors.toList()));

		List<UserRankingResponse> responses = rankingService.getAllUserRankings(LocalDate.now());
		assertEquals(2, responses.size());
		assertEquals(1L, responses.get(0).getUserId());
		assertEquals(3L, responses.get(1).getUserId());
	}

	@Test
	@DisplayName("[getPastWeekUserRanking] 사용자를 찾을 수 없다면 예외가 발생한다.")
	void getPastWeekUserRankingUserNotFound() {
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		AppException exception = assertThrows(AppException.class,
			() -> rankingService.getUserRankInfo(userId, LocalDate.of(2024, 07, 17)));

		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("[getPastWeekUserRanking] 사용자의 과거 랭킹을 조회할 수 있다.")
	void getPastWeekUserRankingTest() {
		Long userId = 1L;
		RankingHistory rankingHistory = RankingHistory.builder()
			.userId(userId)
			.currentPixelCount(10L)
			.ranking(1L)
			.year(2024)
			.week(29)
			.build();

		when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder()
			.id(userId)
			.build()));

		when(rankingHistoryRepository.findByUserIdAndYearAndWeek(userId, 2024, 29))
			.thenReturn(Optional.ofNullable(rankingHistory));

		UserRankingResponse response = rankingService.getUserRankInfo(userId, LocalDate.of(2024, 7, 17));

		Assertions.assertThat(response.getUserId()).isEqualTo(userId);
		Assertions.assertThat(response.getRank()).isEqualTo(1L);
		Assertions.assertThat(response.getCurrentPixelCount()).isEqualTo(10L);
	}

	@Test
	@DisplayName("[getPastWeekUserRanking] 사용자의 과거 랭킹 기록이 없을 때, Null을 반환한다.")
	void getPastWeekUserRankingHistoryNotExists() {
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder()
			.id(userId)
			.build()));

		when(rankingHistoryRepository.findByUserIdAndYearAndWeek(userId, 2024, 29))
			.thenReturn(Optional.empty());

		UserRankingResponse response = rankingService.getUserRankInfo(userId, LocalDate.of(2024, 7, 17));

		Assertions.assertThat(response.getUserId()).isEqualTo(userId);
		Assertions.assertThat(response.getRank()).isEqualTo(null);
		Assertions.assertThat(response.getCurrentPixelCount()).isEqualTo(null);
	}
}
