package com.m3pro.groundflip.domain.dto.achievement;

import java.time.LocalDateTime;

import com.m3pro.groundflip.domain.entity.Achievement;
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
@Schema(title = "업적의 상세 정보")
public class AchievementResponse {
	@Schema(description = "업적 id", example = "1")
	private Long achievementId;

	@Schema(description = "업적 이름", example = "땅 10개 방문")
	private String achievementName;

	@Schema(description = "뱃지 이미지 주소", example = "www.example.com")
	private String badgeImageUrl;

	@Schema(description = "업적 획득 날짜", example = "2024-10-04")
	private LocalDateTime obtainedDate;

	@Schema(description = "카테고리 id", example = "1")
	private Long categoryId;

	@Schema(description = "현재 달성 수치", example = "10")
	private Integer currentValue;

	@Schema(description = "업적 달성 조건", example = "50")
	private Integer completionGoal;

	@Schema(description = "업적 설명", example = "새로운 땅 10개를 방문하면 얻을 수 있습니다")
	private String achievementDetail;

	@Schema(description = "업적 달성 보상", example = "30")
	private Integer reward;

	@Schema(description = "보상을 받았는지 여부", example = "false")
	private Boolean isRewardReceived;

	public static AchievementResponse from(Achievement achievement) {
		return AchievementResponse.builder()
			.achievementId(achievement.getId())
			.achievementName(achievement.getName())
			.badgeImageUrl(achievement.getBadgeImageUrl())
			.obtainedDate(null)
			.categoryId(achievement.getCategoryId())
			.currentValue(0)
			.completionGoal(achievement.getCompletionGoal())
			.achievementDetail(achievement.getDescription())
			.reward(achievement.getRewardAmount())
			.isRewardReceived(false)
			.build();
	}

	public static AchievementResponse from(Achievement achievement, UserAchievement userAchievement) {
		return AchievementResponse.builder()
			.achievementId(achievement.getId())
			.achievementName(achievement.getName())
			.badgeImageUrl(achievement.getBadgeImageUrl())
			.obtainedDate(userAchievement.getObtainedAt())
			.categoryId(achievement.getCategoryId())
			.currentValue(userAchievement.getCurrentValue())
			.completionGoal(achievement.getCompletionGoal())
			.achievementDetail(achievement.getDescription())
			.reward(achievement.getRewardAmount())
			.isRewardReceived(userAchievement.getIsRewardReceived())
			.build();
	}
}
