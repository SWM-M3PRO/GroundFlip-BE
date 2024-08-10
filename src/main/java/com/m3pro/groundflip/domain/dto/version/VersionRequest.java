package com.m3pro.groundflip.domain.dto.version;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "앱 버전 등록")
public class VersionRequest {
	@Schema(description = "앱 버전", example = "1.0.1")
	private String version;
}
