package com.m3pro.groundflip.domain.dto.achievement;

import java.util.List;

import com.m3pro.groundflip.domain.entity.AchievementCategory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "카테고리별 업적 정보")
public class AchievementsByCategoryResponse {
	@Schema(description = "카텍고리 ID", example = "1")
	private Long categoryId;

	@Schema(description = "카테고리 이름", example = "1")
	private String categoryName;

	@Schema(description = "카테고리 대표 이미지 주소", example = "1")
	private String categoryImageUrl;

	@Schema(description = "카텍고리 ID", example = "1")
	private String categoryDescription;

	private List<AchievementElementInterface> achievements;

	public static AchievementsByCategoryResponse from(
		AchievementCategory achievementCategory,
		List<AchievementElementInterface> achievementElements
	) {
		return AchievementsByCategoryResponse.builder()
			.categoryId(achievementCategory.getId())
			.categoryName(achievementCategory.getName())
			.categoryDescription(achievementCategory.getDescription())
			.categoryImageUrl(achievementCategory.getImageUrl())
			.achievements(achievementElements)
			.build();
	}
}
