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
	@Schema(description = "공지 id", example = "1")
	private Long achievementId;

	@Schema(description = "공지 id", example = "1")
	private String achievementName;

	@Schema(description = "공지 id", example = "1")
	private String badgeImageUrl;

	@Schema(description = "공지 id", example = "1")
	private LocalDateTime obtainedDate;

	@Schema(description = "공지 id", example = "1")
	private Long categoryId;
}
