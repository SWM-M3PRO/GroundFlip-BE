package com.m3pro.groundflip.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.auth.AppleLoginRequest;
import com.m3pro.groundflip.domain.dto.auth.LoginRequest;
import com.m3pro.groundflip.domain.dto.auth.LoginResponse;
import com.m3pro.groundflip.domain.dto.auth.LogoutRequest;
import com.m3pro.groundflip.domain.dto.auth.OauthUserInfoResponse;
import com.m3pro.groundflip.domain.dto.auth.ReissueReponse;
import com.m3pro.groundflip.domain.entity.AppleRefreshToken;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.enums.Provider;
import com.m3pro.groundflip.enums.UserStatus;
import com.m3pro.groundflip.jwt.JwtProvider;
import com.m3pro.groundflip.repository.AppleRefreshTokenRepository;
import com.m3pro.groundflip.repository.RankingRedisRepository;
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
	@Mock
	private RankingRedisRepository rankingRedisRepository;
	@Mock
	private AppleRefreshTokenRepository appleRefreshTokenRepository;
	@InjectMocks
	private AuthService authService;

	private Provider provider;
	private LoginRequest loginRequest;
	private AppleLoginRequest appleLoginRequest;
	private OauthUserInfoResponse oauthUserInfo;
	private User existingUser;
	private User newUser;
	private User existingUserNoSignup;

	@BeforeEach
	void init() {
		reset(jwtProvider);
		reset(oauthUserInfoService);
		reset(userRepository);
		provider = Provider.GOOGLE; // Example provider
		loginRequest = new LoginRequest("validAccessToken");
		appleLoginRequest = new AppleLoginRequest("validAccessToken", "validAuthorizationCode");
		oauthUserInfo = new OauthUserInfoResponse() {
			@Override
			public String getEmail() {
				return "test@example.com";
			}

			@Override
			public Provider getOAuthProvider() {
				return provider;
			}
		};
		existingUser = User.builder()
			.id(1L)
			.email("test@example.com")
			.provider(provider)
			.status(UserStatus.COMPLETE)
			.build();
		existingUserNoSignup = User.builder()
			.id(1L)
			.email("test@example.com")
			.provider(provider)
			.status(UserStatus.PENDING)
			.build();
		newUser = User.builder()
			.id(2L)
			.email("newuser@example.com")
			.provider(provider)
			.status(UserStatus.PENDING)
			.build();
	}

	@Test
	@DisplayName("[login] user 가 존재하지 않는 경우 DB 저장하고 토큰을 반환, isSignup은 true")
	public void testLogin_NewUser() {
		when(oauthUserInfoService.requestUserInfo(provider, loginRequest.getAccessToken())).thenReturn(oauthUserInfo);
		when(userRepository.findByProviderAndEmail(provider, oauthUserInfo.getEmail())).thenReturn(Optional.empty());
		when(userRepository.save(any(User.class))).thenReturn(newUser);
		when(jwtProvider.createAccessToken(newUser.getId())).thenReturn("accessToken");
		when(jwtProvider.createRefreshToken(newUser.getId())).thenReturn("refreshToken");

		LoginResponse response = authService.login(provider, loginRequest);

		assertNotNull(response);
		assertEquals("accessToken", response.getAccessToken());
		assertEquals("refreshToken", response.getRefreshToken());
		assertTrue(response.getIsSignUp());

		verify(oauthUserInfoService, times(1)).requestUserInfo(provider, loginRequest.getAccessToken());
		verify(userRepository, times(1)).findByProviderAndEmail(provider, oauthUserInfo.getEmail());
		verify(userRepository, times(1)).save(any(User.class));
		verify(jwtProvider, times(1)).createAccessToken(newUser.getId());
		verify(jwtProvider, times(1)).createRefreshToken(newUser.getId());
		verifyNoMoreInteractions(oauthUserInfoService, userRepository, jwtProvider, rankingRedisRepository);
	}

	@Test
	@DisplayName("[login] DB에 user 가 존재하고 회원가입이 끝났다면 login 성공과 isSignup 은 false")
	public void testLogin_ExistingUser_Signup() {
		when(oauthUserInfoService.requestUserInfo(provider, loginRequest.getAccessToken())).thenReturn(oauthUserInfo);
		when(userRepository.findByProviderAndEmail(provider, oauthUserInfo.getEmail())).thenReturn(
			Optional.of(existingUser));
		when(jwtProvider.createAccessToken(existingUser.getId())).thenReturn("accessToken");
		when(jwtProvider.createRefreshToken(existingUser.getId())).thenReturn("refreshToken");

		LoginResponse response = authService.login(provider, loginRequest);

		assertNotNull(response);
		assertEquals("accessToken", response.getAccessToken());
		assertEquals("refreshToken", response.getRefreshToken());
		assertFalse(response.getIsSignUp());

		verify(oauthUserInfoService, times(1)).requestUserInfo(provider, loginRequest.getAccessToken());
		verify(userRepository, times(1)).findByProviderAndEmail(provider, oauthUserInfo.getEmail());
		verify(jwtProvider, times(1)).createAccessToken(existingUser.getId());
		verify(jwtProvider, times(1)).createRefreshToken(existingUser.getId());
		verifyNoMoreInteractions(oauthUserInfoService, userRepository, jwtProvider);
	}

	@Test
	@DisplayName("[login] DB에 user 가 존재하지만 회원가입이 끝나지 않았다면 login 성공과 isSignup 은 true")
	public void testLogin_ExistingUser_NoSignup() {
		when(oauthUserInfoService.requestUserInfo(provider, loginRequest.getAccessToken())).thenReturn(oauthUserInfo);
		when(userRepository.findByProviderAndEmail(provider, oauthUserInfo.getEmail())).thenReturn(
			Optional.of(existingUserNoSignup));
		when(jwtProvider.createAccessToken(existingUser.getId())).thenReturn("accessToken");
		when(jwtProvider.createRefreshToken(existingUser.getId())).thenReturn("refreshToken");

		LoginResponse response = authService.login(provider, loginRequest);

		assertNotNull(response);
		assertEquals("accessToken", response.getAccessToken());
		assertEquals("refreshToken", response.getRefreshToken());
		assertTrue(response.getIsSignUp());

		verify(oauthUserInfoService, times(1)).requestUserInfo(provider, loginRequest.getAccessToken());
		verify(userRepository, times(1)).findByProviderAndEmail(provider, oauthUserInfo.getEmail());
		verify(jwtProvider, times(1)).createAccessToken(existingUser.getId());
		verify(jwtProvider, times(1)).createRefreshToken(existingUser.getId());
		verifyNoMoreInteractions(oauthUserInfoService, userRepository, jwtProvider);
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

	@Test
	@DisplayName("[loginWithApple] user 가 존재하지 않는 경우 DB 저장하고 토큰을 반환, apple refresh token 저장, isSignup은 true")
	public void testLoginWithApple_NewUser() {
		when(oauthUserInfoService.requestUserInfo(Provider.APPLE, appleLoginRequest.getAccessToken())).thenReturn(
			oauthUserInfo);
		when(oauthUserInfoService.getAppleRefreshToken(appleLoginRequest.getAuthorizationCode())).thenReturn(
			"refresh token");
		when(userRepository.findByProviderAndEmail(Provider.APPLE, oauthUserInfo.getEmail())).thenReturn(
			Optional.empty());
		when(userRepository.save(any(User.class))).thenReturn(newUser);
		when(appleRefreshTokenRepository.save(any(AppleRefreshToken.class))).thenReturn(null);
		when(jwtProvider.createAccessToken(newUser.getId())).thenReturn("accessToken");
		when(jwtProvider.createRefreshToken(newUser.getId())).thenReturn("refreshToken");

		LoginResponse response = authService.loginWithApple(appleLoginRequest);

		assertNotNull(response);
		assertEquals("accessToken", response.getAccessToken());
		assertEquals("refreshToken", response.getRefreshToken());
		assertTrue(response.getIsSignUp());

		verify(oauthUserInfoService, times(1)).requestUserInfo(Provider.APPLE, appleLoginRequest.getAccessToken());
		verify(userRepository, times(1)).findByProviderAndEmail(Provider.APPLE, oauthUserInfo.getEmail());
		verify(userRepository, times(1)).save(any(User.class));
		verify(appleRefreshTokenRepository, times(1)).save(any(AppleRefreshToken.class));
		verify(jwtProvider, times(1)).createAccessToken(newUser.getId());
		verify(jwtProvider, times(1)).createRefreshToken(newUser.getId());
		verifyNoMoreInteractions(oauthUserInfoService, userRepository, jwtProvider, rankingRedisRepository);
	}

	@Test
	@DisplayName("[loginWithApple] DB에 user 가 존재하고 회원가입이 끝났다면 login 성공과 isSignup 은 false")
	public void testLoginWithApple_ExistingUser_Signup() {
		when(oauthUserInfoService.requestUserInfo(Provider.APPLE, appleLoginRequest.getAccessToken())).thenReturn(
			oauthUserInfo);
		when(userRepository.findByProviderAndEmail(Provider.APPLE, oauthUserInfo.getEmail())).thenReturn(
			Optional.of(existingUser));
		when(jwtProvider.createAccessToken(existingUser.getId())).thenReturn("accessToken");
		when(jwtProvider.createRefreshToken(existingUser.getId())).thenReturn("refreshToken");

		LoginResponse response = authService.loginWithApple(appleLoginRequest);

		assertNotNull(response);
		assertEquals("accessToken", response.getAccessToken());
		assertEquals("refreshToken", response.getRefreshToken());
		assertFalse(response.getIsSignUp());

		verify(oauthUserInfoService, times(1)).requestUserInfo(Provider.APPLE, appleLoginRequest.getAccessToken());
		verify(userRepository, times(1)).findByProviderAndEmail(Provider.APPLE, oauthUserInfo.getEmail());
		verify(jwtProvider, times(1)).createAccessToken(existingUser.getId());
		verify(jwtProvider, times(1)).createRefreshToken(existingUser.getId());
		verifyNoMoreInteractions(oauthUserInfoService, userRepository, jwtProvider);
	}

	@Test
	@DisplayName("[loginWithApple] DB에 user 가 존재하지만 회원가입이 끝나지 않았다면 login 성공과 isSignup 은 true")
	public void testLoginWithApple_ExistingUser_NoSignup() {
		when(oauthUserInfoService.requestUserInfo(Provider.APPLE, appleLoginRequest.getAccessToken())).thenReturn(
			oauthUserInfo);
		when(userRepository.findByProviderAndEmail(Provider.APPLE, oauthUserInfo.getEmail())).thenReturn(
			Optional.of(existingUserNoSignup));
		when(jwtProvider.createAccessToken(existingUser.getId())).thenReturn("accessToken");
		when(jwtProvider.createRefreshToken(existingUser.getId())).thenReturn("refreshToken");

		LoginResponse response = authService.loginWithApple(appleLoginRequest);

		assertNotNull(response);
		assertEquals("accessToken", response.getAccessToken());
		assertEquals("refreshToken", response.getRefreshToken());
		assertTrue(response.getIsSignUp());

		verify(oauthUserInfoService, times(1)).requestUserInfo(Provider.APPLE, appleLoginRequest.getAccessToken());
		verify(userRepository, times(1)).findByProviderAndEmail(Provider.APPLE, oauthUserInfo.getEmail());
		verify(jwtProvider, times(1)).createAccessToken(existingUser.getId());
		verify(jwtProvider, times(1)).createRefreshToken(existingUser.getId());
		verifyNoMoreInteractions(oauthUserInfoService, userRepository, jwtProvider);
	}
}
