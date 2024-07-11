package com.m3pro.groundflip.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(title = "로그인 응답")
public class LoginResponse extends Tokens {
	@Schema(description = "최초 로그인(회원가입)인지 여부", example = "true")
	private Boolean isSignUp;

	public LoginResponse(String accessToken, String refreshToken, Boolean isSignUp) {
		super(accessToken, refreshToken);
		this.isSignUp = isSignUp;
	}
}

