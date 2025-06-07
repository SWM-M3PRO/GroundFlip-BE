package com.m3pro.groundflip.scheduler.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.entity.CommunityRankingHistory;
import com.m3pro.groundflip.domain.entity.RankingHistory;
import com.m3pro.groundflip.scheduler.entity.RankingDetail;
import com.m3pro.groundflip.scheduler.repository.SchedulerCommunityRankingHistoryRepository;
import com.m3pro.groundflip.scheduler.repository.SchedulerCommunityRankingRedisRepository;
import com.m3pro.groundflip.scheduler.repository.SchedulerRankingHistoryRepository;
import com.m3pro.groundflip.scheduler.repository.SchedulerUserRankingRedisRepository;
import com.m3pro.groundflip.util.DateUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerRankingService {
	private final SchedulerUserRankingRedisRepository schedulerUserRankingRedisRepository;
	private final SchedulerCommunityRankingRedisRepository schedulerCommunityRankingRedisRepository;
	private final SchedulerRankingHistoryRepository schedulerRankingHistoryRepository;
	private final SchedulerCommunityRankingHistoryRepository schedulerCommunityRankingHistoryRepository;

	@Transactional
	public void transferRankingToDatabase() {
		List<RankingDetail> newRankingDetails = schedulerUserRankingRedisRepository.getRankingsWithCurrentPixelCount();

		Map<Long, RankingHistory> existingRankingHistoryMap = getRankingHistoriesOfThisWeekAsMap();

		List<RankingHistory> updatedRankingHistoryList = new ArrayList<>();

		int existedUser = 0;
		int newUser = 0;
		for (RankingDetail rankingDetail : newRankingDetails) {
			RankingHistory existingRankingHistory = existingRankingHistoryMap.get(rankingDetail.getId());

			if (existingRankingHistory != null) {
				existingRankingHistory.update(rankingDetail.getCurrentPixelCount(), rankingDetail.getRanking());
				updatedRankingHistoryList.add(existingRankingHistory);
				existedUser++;
			} else {
				RankingHistory newRankingHistory = RankingHistory.of(rankingDetail);
				updatedRankingHistoryList.add(newRankingHistory);
				newUser++;
			}
		}

		schedulerRankingHistoryRepository.saveAll(updatedRankingHistoryList);
		log.info("[transferRankingToDatabase] 기존 유저 {}명, 새로운 유저 {}명",
			existedUser,
			newUser
		);
	}

	@Transactional
	public void transferCommunityRankingToDatabase() {
		List<RankingDetail> newRankingDetails = schedulerCommunityRankingRedisRepository.getRankingsWithCurrentPixelCount();

		Map<Long, CommunityRankingHistory> existingRankingHistoryMap = getCommunityRankingHistoriesOfThisWeekAsMap();

		List<CommunityRankingHistory> updatedRankingHistoryList = new ArrayList<>();

		int existedCommunity = 0;
		int newCommunity = 0;
		for (RankingDetail rankingDetail : newRankingDetails) {
			CommunityRankingHistory existingRankingHistory = existingRankingHistoryMap.get(rankingDetail.getId());

			if (existingRankingHistory != null) {
				existingRankingHistory.update(rankingDetail.getCurrentPixelCount(), rankingDetail.getRanking());
				updatedRankingHistoryList.add(existingRankingHistory);
				existedCommunity++;
			} else {
				CommunityRankingHistory newRankingHistory = CommunityRankingHistory.of(rankingDetail);
				updatedRankingHistoryList.add(newRankingHistory);
				newCommunity++;
			}
		}

		schedulerCommunityRankingHistoryRepository.saveAll(updatedRankingHistoryList);
		log.info("[transferCommunityRankingToDatabase] 기존 그룹 {}명, 새로운 그룹 {}명",
			existedCommunity,
			newCommunity
		);
	}

	private Map<Long, CommunityRankingHistory> getCommunityRankingHistoriesOfThisWeekAsMap() {
		LocalDateTime now = LocalDateTime.now();
		Integer currentYear = now.getYear();
		int currentWeek = DateUtils.getWeekOfDate(now.toLocalDate());

		// 월요일 자정 체크
		if (now.getDayOfWeek() == DayOfWeek.MONDAY && now.getHour() == 0) {
			currentWeek -= 1;
		}

		return schedulerCommunityRankingHistoryRepository.findAllByYearAndWeek(currentYear, currentWeek).stream()
			.collect(Collectors.toMap(CommunityRankingHistory::getCommunityId, Function.identity()));
	}

	private Map<Long, RankingHistory> getRankingHistoriesOfThisWeekAsMap() {
		LocalDateTime now = LocalDateTime.now();
		Integer currentYear = now.getYear();
		int currentWeek = DateUtils.getWeekOfDate(now.toLocalDate());

		// 월요일 자정 체크
		if (now.getDayOfWeek() == DayOfWeek.MONDAY && now.getHour() == 0) {
			currentWeek -= 1;
		}

		return schedulerRankingHistoryRepository.findAllByYearAndWeek(currentYear, currentWeek).stream()
			.collect(Collectors.toMap(RankingHistory::getUserId, Function.identity()));
	}

	public void resetUserRanking() {
		schedulerUserRankingRedisRepository.resetAllScoresToZero();
	}

	public void resetCommunityRanking() {
		schedulerCommunityRankingRedisRepository.resetAllScoresToZero();
	}

}
