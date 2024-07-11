package com.m3pro.groundflip.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(title = "토큰 재발급 요청 Body")
public class ReissueRequest {
	@Schema(description = "refresh token", example = "dslafjkdsrtjlejldfkajlasljdf")
	private String refreshToken;
}

