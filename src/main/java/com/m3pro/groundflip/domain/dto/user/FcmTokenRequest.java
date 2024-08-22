package com.m3pro.groundflip.domain.dto.user;

import com.m3pro.groundflip.enums.Device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "FCM 등록 토큰 저장")
public class FcmTokenRequest {
	@Schema(description = "사용자 Id", example = "125")
	private Long userId;

	@Schema(description = "사용자 fcm token", example = "sdfghweredasdvasdfq/weqwefs;dvsdghrthwdffevdrer")
	private String fcmToken;

	@Schema(description = "사용자 기기 종류 (iOS, Android)", example = "iOS")
	private Device device;
}
