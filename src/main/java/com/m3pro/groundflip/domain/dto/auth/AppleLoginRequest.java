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
@Schema(title = "애플 로그인 요청 Body")
public class AppleLoginRequest {
	// Todo: access token 을 클라이언트와 같이 identity token 으로 변경해야함
	@Schema(description = "애플 identity token", example = "dslafjkdsrtjlejldfkajlasljdf")
	private String accessToken;

	@Schema(description = "애플 authorization code", example = "dslafjkdsrtjlejldfkajlasljdf")
	private String authorizationCode;
}
