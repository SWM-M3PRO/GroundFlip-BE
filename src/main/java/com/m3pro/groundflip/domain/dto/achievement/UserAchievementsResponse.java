package com.m3pro.groundflip.domain.dto.achievement;

import java.util.List;

import com.m3pro.groundflip.domain.entity.UserAchievement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "내 업적 정보")
public class UserAchievementsResponse {
	@Schema(description = "공지 제목", example = "제목")
	private Long achievementCount;
	private List<AchievementElement> recentAchievements;

	public static UserAchievementsResponse from(List<UserAchievement> achievements, Long count) {
		List<AchievementElement> achievementElements = achievements.stream()
			.map((achievement) -> AchievementElement.builder()
				.achievementId(achievement.getAchievement().getId())
				.achievementName(achievement.getAchievement().getName())
				.badgeImageUrl(achievement.getAchievement().getBadgeImageUrl())
				.obtainedDate(achievement.getObtainedAt())
				.categoryId(achievement.getAchievement().getAchievementCategory().getId())
				.build()
			).toList();

		return UserAchievementsResponse.builder()
			.achievementCount(count)
			.recentAchievements(achievementElements)
			.build();
	}
}
