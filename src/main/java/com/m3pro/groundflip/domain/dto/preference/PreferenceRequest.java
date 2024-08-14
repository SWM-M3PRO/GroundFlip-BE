package com.m3pro.groundflip.domain.dto.preference;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(title = "권한 동의 Body")
public class PreferenceRequest {
	@Schema(description = "사용자 ID", example = "5")
	private Long userId;

	@Schema(description = "권한 동의 여부", example = "true")
	private boolean isEnabled;

}
