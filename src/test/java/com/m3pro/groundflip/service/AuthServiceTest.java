package com.m3pro.groundflip.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.auth.LogoutRequest;
import com.m3pro.groundflip.domain.dto.auth.ReissueReponse;
import com.m3pro.groundflip.jwt.JwtProvider;
import com.m3pro.groundflip.repository.UserRepository;
import com.m3pro.groundflip.service.oauth.OauthService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
	@Mock
	private OauthService oauthUserInfoService;
	@Mock
	private JwtProvider jwtProvider;
	@Mock
	private UserRepository userRepository;
	@InjectMocks
	private AuthService authService;

	@BeforeEach
	void init() {
		reset(jwtProvider);
		reset(oauthUserInfoService);
		reset(userRepository);
	}

	@Test
	@DisplayName("[logout] 정상적으로 로그아웃 실행")
	void logoutTest() {
		// Given
		String accessToken = "accessToken";
		String refreshToken = "refreshToken";
		LogoutRequest logoutRequest = new LogoutRequest(accessToken, refreshToken);

		// When
		authService.logout(logoutRequest);

		// Then
		verify(jwtProvider, times(1)).expireToken(accessToken);
		verify(jwtProvider, times(1)).expireToken(refreshToken);
	}

	@Test
	@DisplayName("[reissueToken] 정상적으로 토큰 재발급")
	public void reissueTokenTest() {
		// Given
		String refreshToken = "validRefreshToken";
		Long userId = 123L;
		String newAccessToken = "newAccessToken";
		String newRefreshToken = "newRefreshToken";

		doNothing().when(jwtProvider).validateToken(refreshToken);
		doNothing().when(jwtProvider).expireToken(refreshToken);
		when(jwtProvider.parseUserId(refreshToken)).thenReturn(userId);
		when(jwtProvider.createAccessToken(userId)).thenReturn(newAccessToken);
		when(jwtProvider.createRefreshToken(userId)).thenReturn(newRefreshToken);

		// When
		ReissueReponse response = authService.reissueToken(refreshToken);

		// Then
		verify(jwtProvider, times(1)).validateToken(refreshToken);
		verify(jwtProvider, times(1)).expireToken(refreshToken);
		verify(jwtProvider, times(1)).parseUserId(refreshToken);
		verify(jwtProvider, times(1)).createAccessToken(userId);
		verify(jwtProvider, times(1)).createRefreshToken(userId);

		assertNotNull(response);
		assertEquals(newAccessToken, response.getAccessToken());
		assertEquals(newRefreshToken, response.getRefreshToken());
	}
}
