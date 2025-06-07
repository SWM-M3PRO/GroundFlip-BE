package com.m3pro.groundflip.scheduler.service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m3pro.groundflip.domain.entity.RankingHistory;
import com.m3pro.groundflip.domain.entity.UserAchievement;
import com.m3pro.groundflip.enums.AchievementCategoryId;
import com.m3pro.groundflip.scheduler.enums.Ranking;
import com.m3pro.groundflip.scheduler.repository.SchedulerAchievementRepository;
import com.m3pro.groundflip.scheduler.repository.SchedulerRankingHistoryRepository;
import com.m3pro.groundflip.scheduler.repository.SchedulerUserAchievementRepository;
import com.m3pro.groundflip.scheduler.repository.SchedulerUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerAchievementService {
	private final SchedulerRankingHistoryRepository schedulerRankingHistoryRepository;
	private final SchedulerUserAchievementRepository schedulerUserAchievementRepository;
	private final SchedulerAchievementRepository schedulerAchievementRepository;
	private final SchedulerUserRepository userRepository;

	private final SchedulerNotificationService schedulerNotificationService;

	private static List<Long> getRankingAchievementId(Long ranking) {
		return EnumSet.allOf(Ranking.class).stream()
			.filter(r -> ranking <= r.getRanking())
			.map(Ranking::getAchievementId)
			.toList();
	}

	@Transactional
	public void updateRankingAchievement(Integer year, Integer week) {
		List<RankingHistory> rankingHistories = schedulerRankingHistoryRepository.findAllByYearAndWeek(year, week);

		rankingHistories.forEach(rankingHistory -> {
			Long userId = rankingHistory.getUserId();
			Long ranking = rankingHistory.getRanking();

			List<Long> achievementIds = getRankingAchievementId(ranking);
			List<UserAchievement> userAchievements = schedulerUserAchievementRepository
				.findAllByUserIdAndCategoryId(userId, AchievementCategoryId.RANKER.getCategoryId());

			updateRankingAchievements(achievementIds, userAchievements, userId);
		});
	}

	private void updateRankingAchievements(List<Long> achievementIds, List<UserAchievement> userAchievements,
		Long userId) {
		for (Long achievementId : achievementIds) {
			boolean alreadyHasAchievement = userAchievements.stream()
				.anyMatch(ua -> ua.getAchievement().getId().equals(achievementId));

			if (!alreadyHasAchievement) {
				UserAchievement newAchievement = UserAchievement.builder()
					.user(userRepository.getReferenceById(userId))
					.achievement(schedulerAchievementRepository.getReferenceById(achievementId))
					.currentValue(1)
					.obtainedAt(LocalDateTime.now())
					.isRewardReceived(false)
					.build();
				schedulerUserAchievementRepository.save(newAchievement);
				schedulerNotificationService.createAchievementNotification(userId,
					schedulerAchievementRepository.getReferenceById(achievementId));
			}
		}
	}
}
