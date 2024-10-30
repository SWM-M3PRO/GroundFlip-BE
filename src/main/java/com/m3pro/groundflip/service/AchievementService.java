package com.m3pro.groundflip.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.achievement.AchievementCategoryInfoResponse;
import com.m3pro.groundflip.domain.dto.achievement.AchievementElementInterface;
import com.m3pro.groundflip.domain.dto.achievement.AchievementResponse;
import com.m3pro.groundflip.domain.dto.achievement.AchievementsByCategoryResponse;
import com.m3pro.groundflip.domain.dto.achievement.UserAchievementsResponse;
import com.m3pro.groundflip.domain.entity.Achievement;
import com.m3pro.groundflip.domain.entity.AchievementCategory;
import com.m3pro.groundflip.domain.entity.UserAchievement;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.AchievementCategoryRepository;
import com.m3pro.groundflip.repository.AchievementRepository;
import com.m3pro.groundflip.repository.UserAchievementRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {
	private final AchievementRepository achievementRepository;
	private final AchievementCategoryRepository achievementCategoryRepository;
	private final UserAchievementRepository userAchievementRepository;

	public UserAchievementsResponse getUserAchievements(Long userId, Integer count) {
		Long achievementCount = userAchievementRepository.countByUserId(userId);
		List<UserAchievement> userAchievements;
		if (count == null) {
			userAchievements = userAchievementRepository.findAllByUserId(userId);
		} else {
			userAchievements = userAchievementRepository.findAllByUserId(userId, PageRequest.of(0, count));
		}
		return UserAchievementsResponse.from(userAchievements, achievementCount);
	}

	public AchievementResponse getAchievement(Long achievementId, Long userId) {
		Achievement achievement = achievementRepository.findById(achievementId).orElseThrow(() -> new AppException(
			ErrorCode.ACHIEVEMENT_NOT_FOUND));
		Optional<UserAchievement> userAchievement = userAchievementRepository.findByAchievementAndUserId(
			achievementRepository.getReferenceById(achievementId), userId);
		if (userAchievement.isPresent()) {
			return AchievementResponse.from(achievement, userAchievement.get());
		} else {
			return AchievementResponse.from(achievement);
		}
	}

	public List<AchievementCategoryInfoResponse> getAchievementCategories() {
		List<AchievementCategory> achievementCategories = achievementCategoryRepository.findAll();
		return achievementCategories.stream()
			.map((achievementCategory) -> AchievementCategoryInfoResponse.builder()
				.categoryId(achievementCategory.getId())
				.categoryName(achievementCategory.getName())
				.categoryImageUrl(achievementCategory.getImageUrl())
				.build()
			).toList();
	}

	public AchievementsByCategoryResponse getAchievementsByCategory(Long achievementCategoryId, Long userId) {
		AchievementCategory achievementCategory = achievementCategoryRepository
			.findById(achievementCategoryId)
			.orElseThrow(() -> new AppException(ErrorCode.ACHIEVEMENT_NOT_FOUND));
		List<AchievementElementInterface> achievementElementsByCategory = achievementRepository.findAllByCategory(
			achievementCategoryId, userId);

		return AchievementsByCategoryResponse.from(achievementCategory, achievementElementsByCategory);
	}
}
