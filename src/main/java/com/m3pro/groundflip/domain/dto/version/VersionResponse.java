package com.m3pro.groundflip.domain.dto.version;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "앱 버전 get")
public class VersionResponse {
	@Schema(description = "앱 버전", example = "1.0.5")
	private String version;

	@Schema(description = "업데이트 필요 여부", example = "0")
	private Integer needUpdate;

	@Schema(description = "생성날짜", example = "2024-08-11 01:37:22.372436")
	private LocalDateTime createdDate;
}
