package com.m3pro.groundflip.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.ranking.CommunityRankingResponse;
import com.m3pro.groundflip.domain.dto.ranking.Ranking;
import com.m3pro.groundflip.domain.entity.Community;
import com.m3pro.groundflip.domain.entity.CommunityRankingHistory;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.CommunityRankingHistoryRepository;
import com.m3pro.groundflip.repository.CommunityRankingRedisRepository;
import com.m3pro.groundflip.repository.CommunityRepository;
import com.m3pro.groundflip.util.DateUtils;

@ExtendWith(MockitoExtension.class)
public class CommunityRankingServiceTest {
	@Mock
	private CommunityRankingRedisRepository communityRankingRedisRepository;
	@Mock
	private CommunityRepository communityRepository;
	@Mock
	private CommunityRankingHistoryRepository communityRankingHistoryRepository;
	@InjectMocks
	private CommunityRankingService communityRankingService;

	@BeforeEach
	void init() {
		reset(communityRankingRedisRepository);
	}

	@Test
	@DisplayName("[updateRanking] Same community already owns pixel and it's from this week, no ranking update")
	void updateRanking_NoUpdateCurrentPixel() {
		Long occupyingCommunityId = 1L;
		Pixel targetPixel = Pixel.builder().communityId(occupyingCommunityId).build();
		targetPixel.updateCommunityOccupiedAt(DateUtils.getThisWeekStartDate().atTime(0, 0, 0).plusDays(1));

		communityRankingService.updateCurrentPixelRanking(targetPixel, occupyingCommunityId);

		verify(communityRankingRedisRepository, never()).increaseCurrentPixelCount(occupyingCommunityId);
	}

	@Test
	@DisplayName("[updateRanking] Community owned the pixel last week, ranking updated")
	void updateRanking_UpdateCurrentPixelBeforeThisWeek() {
		Long occupyingCommunityId = 1L;
		Pixel targetPixel = Pixel.builder().communityId(occupyingCommunityId).build();
		targetPixel.updateCommunityOccupiedAt(DateUtils.getThisWeekStartDate().atTime(0, 0, 0).minusDays(3));

		communityRankingService.updateCurrentPixelRanking(targetPixel, occupyingCommunityId);

		verify(communityRankingRedisRepository, times(1)).increaseCurrentPixelCount(occupyingCommunityId);
	}

	@Test
	@DisplayName("[updateRanking] Pixel never owned, adds to occupying community's score")
	void updateCurrentPixelRanking_NeverOccupied() {
		Long occupyingCommunityId = 1L;
		Pixel targetPixel = Pixel.builder().communityId(null).build();

		communityRankingService.updateCurrentPixelRanking(targetPixel, occupyingCommunityId);

		verify(communityRankingRedisRepository, times(1)).increaseCurrentPixelCount(occupyingCommunityId);
	}

	@Test
	@DisplayName("[updateRankingAfterOccupy] Increases score for occupying community, decreases for deprived")
	void updateCurrentPixelRankingAfterOccupyTest() {
		Long occupyingCommunityId = 1L;
		Long deprivedCommunityId = 2L;

		communityRankingService.updateCurrentPixelRankingAfterOccupy(occupyingCommunityId, deprivedCommunityId);

		verify(communityRankingRedisRepository, times(1)).decreaseCurrentPixelCount(deprivedCommunityId);
		verify(communityRankingRedisRepository, times(1)).increaseCurrentPixelCount(occupyingCommunityId);
	}

	@Test
	@DisplayName("[getCurrentPixelCount] Returns community's pixel count")
	void getCurrentPixelCountFromCacheTest() {
		Long communityId = 1L;
		when(communityRankingRedisRepository.getCurrentPixelCount(any())).thenReturn(Optional.of(15L));

		Long count = communityRankingService.getCurrentPixelCountFromCache(communityId);

		assertThat(count).isEqualTo(15L);
	}

	@Test
	@DisplayName("[getCurrentPixelCount] Returns 0 if community not in Redis")
	void getCurrentPixelCountFromCacheTestEmpty() {
		Long communityId = 1L;
		when(communityRankingRedisRepository.getCurrentPixelCount(any())).thenReturn(Optional.empty());

		Long count = communityRankingService.getCurrentPixelCountFromCache(communityId);

		assertThat(count).isEqualTo(0L);
	}

	@Test
	@DisplayName("[getCommunityRankInfo] Community not found, throws exception")
	void getCommunityRankInfo_NotFound() {
		Long communityId = 1L;
		when(communityRepository.findById(communityId)).thenReturn(Optional.empty());

		AppException exception = assertThrows(AppException.class,
			() -> communityRankingService.getCommunityCurrentPixelRankInfo(communityId, LocalDate.now()));

		assertEquals(ErrorCode.COMMUNITY_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("[getCommunityRankInfo] Returns current community ranking")
	void getCommunityCurrentPixelRankFromCacheInfoTest() {
		Long communityId = 1L;
		Community community = Community.builder()
			.id(communityId)
			.name("Community1")
			.build();
		when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
		when(communityRankingRedisRepository.getCurrentPixelCount(any())).thenReturn(Optional.of(15L));
		when(communityRankingRedisRepository.getCurrentPixelRank(any())).thenReturn(Optional.of(1L));

		CommunityRankingResponse response = communityRankingService.getCommunityCurrentPixelRankInfo(communityId,
			LocalDate.now());

		assertThat(response.getCommunityId()).isEqualTo(communityId);
		assertThat(response.getCurrentPixelCount()).isEqualTo(15L);
		assertThat(response.getRank()).isEqualTo(1L);
	}

	@Test
	@DisplayName("[getCommunityRankInfo] Lookup date is null, defaults to current date and uses current week logic")
	void getCommunityCurrentPixelRankInfo_LookUpDateIsNull() {
		Long communityId = 1L;
		Community community = Community.builder()
			.id(communityId)
			.name("Community1")
			.build();

		when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
		when(communityRankingRedisRepository.getCurrentPixelCount(any())).thenReturn(Optional.of(15L));
		when(communityRankingRedisRepository.getCurrentPixelRank(any())).thenReturn(Optional.of(1L));

		CommunityRankingResponse response = communityRankingService.getCommunityCurrentPixelRankInfo(communityId, null);

		assertThat(response.getCommunityId()).isEqualTo(communityId);
		assertThat(response.getCurrentPixelCount()).isEqualTo(15L);
		assertThat(response.getRank()).isEqualTo(1L);
	}

	@Test
	@DisplayName("[getCommunityRankInfo] Lookup date is in the current week, returns current ranking")
	void getCommunityCurrentPixelRankInfo_CurrentWeek() {
		Long communityId = 1L;
		Community community = Community.builder()
			.id(communityId)
			.name("Community1")
			.build();

		when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
		when(communityRankingRedisRepository.getCurrentPixelCount(any())).thenReturn(Optional.of(20L));
		when(communityRankingRedisRepository.getCurrentPixelRank(any())).thenReturn(Optional.of(5L));

		LocalDate currentWeekDate = DateUtils.getThisWeekStartDate().plusDays(1);
		CommunityRankingResponse response = communityRankingService.getCommunityCurrentPixelRankInfo(communityId,
			currentWeekDate);

		assertThat(response.getCommunityId()).isEqualTo(communityId);
		assertThat(response.getCurrentPixelCount()).isEqualTo(20L);
		assertThat(response.getRank()).isEqualTo(5L);
	}

	@Test
	@DisplayName("[getCommunityRankInfo] Lookup date is in a past week, returns past ranking from history")
	void getCommunityCurrentPixelRankInfo_PastWeek() {
		Long communityId = 1L;
		Community community = Community.builder()
			.id(communityId)
			.name("Community1")
			.build();

		when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

		CommunityRankingHistory rankingHistory = CommunityRankingHistory.builder()
			.communityId(communityId)
			.currentPixelCount(50L)
			.ranking(2L)
			.year(2023)
			.week(10)
			.build();

		when(communityRankingHistoryRepository.findByCommunityIdAndYearAndWeek(anyLong(), anyInt(), anyInt()))
			.thenReturn(Optional.of(rankingHistory));

		LocalDate pastDate = LocalDate.of(2023, 3, 7); // Assuming this is in week 10 of 2023
		CommunityRankingResponse response = communityRankingService.getCommunityCurrentPixelRankInfo(communityId,
			pastDate);

		assertThat(response.getCommunityId()).isEqualTo(communityId);
		assertThat(response.getCurrentPixelCount()).isEqualTo(50L);
		assertThat(response.getRank()).isEqualTo(2L);
	}

	@Test
	@DisplayName("[getCommunityRankInfo] Lookup date is in a past week, no history found, returns community without ranking")
	void getCommunityCurrentPixelRankInfo_PastWeekNoHistory() {
		Long communityId = 1L;
		Community community = Community.builder()
			.id(communityId)
			.name("Community1")
			.build();

		when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
		when(communityRankingHistoryRepository.findByCommunityIdAndYearAndWeek(anyLong(), anyInt(), anyInt()))
			.thenReturn(Optional.empty());

		LocalDate pastDate = LocalDate.of(2023, 5, 10); // Assuming this is a past date
		CommunityRankingResponse response = communityRankingService.getCommunityCurrentPixelRankInfo(communityId,
			pastDate);

		assertThat(response.getCommunityId()).isEqualTo(communityId);
		assertNull(response.getRank());
		assertNull(response.getCurrentPixelCount());
	}

	@Test
	@DisplayName("[getCommunityRankInfo] Current week community not found in Redis, throws exception")
	void getCommunityCurrentPixelRankInfo_CurrentWeekCommunityNotFoundInRedis() {
		Long communityId = 1L;
		when(communityRepository.findById(communityId)).thenReturn(Optional.of(Community.builder()
			.id(communityId)
			.name("Community1")
			.build()));

		when(communityRankingRedisRepository.getCurrentPixelRank(communityId)).thenReturn(Optional.empty());

		AppException exception = assertThrows(AppException.class,
			() -> communityRankingService.getCommunityCurrentPixelRankInfo(communityId, LocalDate.now()));

		assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
	}

	@Test
	@DisplayName("[getAllCommunityRanking] Fetches top 30 community rankings")
	void getAllCurrentWeekRankingTest() {
		List<Ranking> rankings = Arrays.asList(
			new Ranking(1L, 10L, 3L),
			new Ranking(2L, 20L, 1L),
			new Ranking(3L, 15L, 2L)
		);

		List<Community> communities = Arrays.asList(
			Community.builder().id(1L).name("Community1").build(),
			Community.builder().id(2L).name("Community2").build(),
			Community.builder().id(3L).name("Community3").build()
		);

		when(communityRankingRedisRepository.getRankingsWithCurrentPixelCount()).thenReturn(rankings);
		when(communityRepository.findAllById(anySet())).thenReturn(communities);

		List<CommunityRankingResponse> responses = communityRankingService.getCurrentPixelAllUCommunityRankings(
			LocalDate.now());

		assertEquals(3, responses.size());
		assertEquals(1L, responses.get(0).getCommunityId());
		assertEquals(2L, responses.get(1).getCommunityId());
		assertEquals(3L, responses.get(2).getCommunityId());
	}

	@Test
	@DisplayName("[getCurrentPixelAllUCommunityRankings] Lookup date is null, defaults to current date and fetches current week rankings")
	void getCurrentPixelAllUCommunityRankings_LookUpDateIsNull() {
		List<Ranking> rankings = Arrays.asList(
			new Ranking(1L, 10L, 3L),
			new Ranking(2L, 20L, 1L)
		);

		List<Community> communities = Arrays.asList(
			Community.builder().id(1L).name("Community1").build(),
			Community.builder().id(2L).name("Community2").build()
		);

		when(communityRankingRedisRepository.getRankingsWithCurrentPixelCount()).thenReturn(rankings);
		when(communityRepository.findAllById(anySet())).thenReturn(communities);

		List<CommunityRankingResponse> responses = communityRankingService.getCurrentPixelAllUCommunityRankings(null);

		assertEquals(2, responses.size());
		assertEquals(1L, responses.get(0).getCommunityId());
		assertEquals(2L, responses.get(1).getCommunityId());
	}

	@Test
	@DisplayName("[getCurrentPixelAllUCommunityRankings] Lookup date is in the current week, returns current week rankings")
	void getCurrentPixelAllUCommunityRankings_CurrentWeek() {
		List<Ranking> rankings = Arrays.asList(
			new Ranking(1L, 10L, 3L),
			new Ranking(2L, 20L, 1L)
		);

		List<Community> communities = Arrays.asList(
			Community.builder().id(1L).name("Community1").build(),
			Community.builder().id(2L).name("Community2").build()
		);

		when(communityRankingRedisRepository.getRankingsWithCurrentPixelCount()).thenReturn(rankings);
		when(communityRepository.findAllById(anySet())).thenReturn(communities);

		LocalDate currentWeekDate = DateUtils.getThisWeekStartDate().plusDays(1);
		List<CommunityRankingResponse> responses = communityRankingService.getCurrentPixelAllUCommunityRankings(
			currentWeekDate);

		assertEquals(2, responses.size());
		assertEquals(1L, responses.get(0).getCommunityId());
		assertEquals(2L, responses.get(1).getCommunityId());
	}

	@Test
	@DisplayName("[getCurrentPixelAllUCommunityRankings] Lookup date is in a past week, returns past week rankings")
	void getCurrentPixelAllUCommunityRankings_PastWeek() {
		when(communityRankingHistoryRepository.findAllByYearAndWeek(anyInt(), anyInt()))
			.thenReturn(Arrays.asList(
				CommunityRankingResponse.from(Community.builder().id(1L).name("Community1").build(), 2L, 50L),
				CommunityRankingResponse.from(Community.builder().id(2L).name("Community2").build(), 1L, 30L)
			));

		LocalDate pastDate = LocalDate.of(2023, 3, 7); // Assuming this is in week 10 of 2023
		List<CommunityRankingResponse> responses = communityRankingService.getCurrentPixelAllUCommunityRankings(
			pastDate);

		assertEquals(2, responses.size());
		assertEquals(1L, responses.get(0).getCommunityId());
		assertEquals(2L, responses.get(1).getCommunityId());
	}

	@Test
	@DisplayName("[getCurrentPixelAllUCommunityRankings] Lookup date is in a past week, no ranking history found")
	void getCurrentPixelAllUCommunityRankings_PastWeekNoHistory() {
		when(communityRankingHistoryRepository.findAllByYearAndWeek(anyInt(), anyInt()))
			.thenReturn(List.of());

		LocalDate pastDate = LocalDate.of(2023, 5, 10); // Assuming this is a past date
		List<CommunityRankingResponse> responses = communityRankingService.getCurrentPixelAllUCommunityRankings(
			pastDate);

		assertEquals(0, responses.size());
	}

	@Test
	@DisplayName("[getCurrentPixelAllUCommunityRankings] No rankings available in Redis for the current week")
	void getCurrentPixelAllUCommunityRankings_NoRankingsInRedis() {
		when(communityRankingRedisRepository.getRankingsWithCurrentPixelCount()).thenReturn(List.of());

		List<CommunityRankingResponse> responses = communityRankingService.getCurrentPixelAllUCommunityRankings(
			LocalDate.now());

		assertEquals(0, responses.size());
	}

	@Test
	@DisplayName("[getCurrentPixelAllUCommunityRankings] Filters out communities not found in database")
	void getCurrentPixelAllUCommunityRankings_FilterMissingCommunities() {
		List<Ranking> rankings = Arrays.asList(
			new Ranking(1L, 10L, 3L),
			new Ranking(2L, 20L, 1L),
			new Ranking(3L, 15L, 2L) // This community will not be found in the repository
		);

		List<Community> communities = Arrays.asList(
			Community.builder().id(1L).name("Community1").build(),
			Community.builder().id(2L).name("Community2").build()
		);

		when(communityRankingRedisRepository.getRankingsWithCurrentPixelCount()).thenReturn(rankings);
		when(communityRepository.findAllById(anySet())).thenReturn(communities);

		List<CommunityRankingResponse> responses = communityRankingService.getCurrentPixelAllUCommunityRankings(
			LocalDate.now());

		// We expect only the two existing communities to be returned
		assertEquals(2, responses.size());
		assertEquals(1L, responses.get(0).getCommunityId());
		assertEquals(2L, responses.get(1).getCommunityId());
	}

	@Test
	@DisplayName("[updateAccumulatedRanking]")
	void updateAccumulatedRankingTest() {
		Long communityId = 1L;

		communityRankingService.updateAccumulatedRanking(communityId);

		verify(communityRankingRedisRepository, times(1)).increaseAccumulatePixelCount(communityId);
	}

	@Test
	@DisplayName("[getAccumulatePixelCount]")
	void getAccumulatePixelCountTest() {
		Long communityId = 1L;
		when(communityRankingRedisRepository.getAccumulatePixelCount(any())).thenReturn(Optional.of(15L));

		Long count = communityRankingService.getAccumulatePixelCount(communityId);

		assertThat(count).isEqualTo(15L);
	}
}
