package com.m3pro.groundflip.domain.dto.achievement;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "업적 리스트의 정보")
public class AchievementElement {
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
}
