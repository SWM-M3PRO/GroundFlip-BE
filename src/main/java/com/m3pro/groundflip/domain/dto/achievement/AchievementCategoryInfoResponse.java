package com.m3pro.groundflip.domain.dto.achievement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "카테고리 리스트의 정보")
public class AchievementCategoryInfoResponse {
	@Schema(description = "카테고리 id", example = "1")
	private Long categoryId;

	@Schema(description = "카테고리 이름", example = "탐험왕")
	private String categoryName;

	@Schema(description = "카테고리 대표 이미지 주소", example = "www.example.com")
	private String categoryImageUrl;
}
