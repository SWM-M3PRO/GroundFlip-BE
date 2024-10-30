package com.m3pro.groundflip.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m3pro.groundflip.domain.entity.Achievement;
import com.m3pro.groundflip.domain.entity.UserAchievement;
import com.m3pro.groundflip.enums.AchievementCategoryId;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.AchievementRepository;
import com.m3pro.groundflip.repository.UserAchievementRepository;
import com.m3pro.groundflip.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementManager {
	private final AchievementRepository achievementRepository;
	private final UserAchievementRepository userAchievementRepository;
	private final UserRepository userRepository;

	@Transactional
	public void updateAccumulateAchievement(Long userId, AchievementCategoryId categoryId) {
		UserAchievement achievementToUpdate = getAchievementToUpdate(categoryId.getCategoryId(), userId);

		achievementToUpdate.increaseCurrentValue();

		if (Objects.equals(achievementToUpdate.getCurrentValue(),
			achievementToUpdate.getAchievement().getCompletionGoal())) {
			completeAchievement(achievementToUpdate, userId);
		}
	}

	private void completeAchievement(UserAchievement achievementToUpdate, Long userId) {
		achievementToUpdate.setObtainedAt();
		Long nextAchievementId = achievementToUpdate.getAchievement().getNextAchievementId();
		if (nextAchievementId != null) {
			UserAchievement nextAchievement = UserAchievement.builder()
				.achievement(achievementRepository.getReferenceById(nextAchievementId))
				.user(userRepository.getReferenceById(userId))
				.currentValue(achievementToUpdate.getCurrentValue())
				.isRewardReceived(false)
				.build();
			userAchievementRepository.save(nextAchievement);
		}
	}

	private UserAchievement getAchievementToUpdate(Long categoryId, Long userId) {
		List<UserAchievement> userAchievements = userAchievementRepository
			.findAllByUserIdAndCategoryId(userId, categoryId);

		if (userAchievements.isEmpty()) {
			Achievement achievement = achievementRepository
				.findByCategoryId(categoryId).orElseThrow(() -> new AppException(ErrorCode.ACHIEVEMENT_NOT_FOUND));

			UserAchievement nextAchievement = UserAchievement.builder()
				.achievement(achievement)
				.user(userRepository.getReferenceById(userId))
				.currentValue(0)
				.isRewardReceived(false)
				.build();
			return userAchievementRepository.save(nextAchievement);
		} else {
			return userAchievements.get(userAchievements.size() - 1);
		}
	}
}