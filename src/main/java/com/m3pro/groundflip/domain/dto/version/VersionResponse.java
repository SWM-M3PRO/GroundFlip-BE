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
@Schema(title = "앱 버전 get")
public class VersionResponse {
	@Schema(description = "앱 버전", example = "1.0.5")
	private String version;
}
