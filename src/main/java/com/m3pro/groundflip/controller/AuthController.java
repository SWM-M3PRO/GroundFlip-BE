package com.m3pro.groundflip.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.auth.AppleLoginRequest;
import com.m3pro.groundflip.domain.dto.auth.LoginRequest;
import com.m3pro.groundflip.domain.dto.auth.LoginResponse;
import com.m3pro.groundflip.domain.dto.auth.LogoutRequest;
import com.m3pro.groundflip.domain.dto.auth.ReissueReponse;
import com.m3pro.groundflip.domain.dto.auth.ReissueRequest;
import com.m3pro.groundflip.enums.Provider;
import com.m3pro.groundflip.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "auth", description = "인증 인가 API")
public class AuthController {
	private final AuthService authService;

	@Operation(summary = "카카오 로그인", description = "카카오에서 받은 액세스 토큰을 통해 회원가입 또는 로그인하는 API")
	@PostMapping("/kakao/login")
	public Response<LoginResponse> loginKaKao(@RequestBody LoginRequest loginRequest) {
		return Response.createSuccess(authService.login(Provider.KAKAO, loginRequest));
	}

	@Operation(summary = "애플 로그인", description = "애플에서 받은 identity token을 통해 회원가입 또는 로그인하는 API")
	@PostMapping("/apple/login")
	public Response<LoginResponse> loginApple(@RequestBody AppleLoginRequest appleLoginRequest) {
		return Response.createSuccess(authService.loginWithApple(appleLoginRequest));
	}

	@Operation(summary = "access token 재발급", description = "만료된 access token 을 refresh token으로 재발급 하는 API")
	@PostMapping("/reissue")
	public Response<ReissueReponse> reissueToken(@RequestBody ReissueRequest reissueRequest) {
		return Response.createSuccess(authService.reissueToken(reissueRequest.getRefreshToken()));
	}

	@Operation(summary = "로그아웃", description = "로그아웃 하는 API access token과 refresh token 을 body에 담아 보내고 서버에서 토큰을 만료시킨다.")
	@PostMapping("/logout")
	public Response<?> logout(@RequestBody LogoutRequest logoutRequest) {
		authService.logout(logoutRequest);
		return Response.createSuccessWithNoData();
	}
}

