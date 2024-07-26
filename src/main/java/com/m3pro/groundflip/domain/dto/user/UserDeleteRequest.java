package com.m3pro.groundflip.domain.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "사용자 탈퇴")
public class UserDeleteRequest {
	private String accessToken;
	private String refreshToken;
}
