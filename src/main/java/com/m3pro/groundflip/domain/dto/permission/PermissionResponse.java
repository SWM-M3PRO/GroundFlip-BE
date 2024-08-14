package com.m3pro.groundflip.domain.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(title = "권한 정보 body")
public class PermissionResponse {
	@Schema(description = "서비스 알림 동의 여부", example = "true")
	private boolean isServiceNotificationEnabled;

	@Schema(description = "마케팅 알림 동의 여부", example = "true")
	private boolean isMarketingNotificationEnabled;
}
