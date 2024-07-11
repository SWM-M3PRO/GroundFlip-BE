package com.m3pro.groundflip.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(title = "토큰 재발급 응답")
public class ReissueReponse {
	@Schema(description = "서버에서 재발급한 액세스 토큰")
	private String accessToken;

	@Schema(description = "서버에서 재발급한 리프레쉬 토큰")
	private String refreshToken;
}
