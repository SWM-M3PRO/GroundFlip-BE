package com.m3pro.groundflip.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.m3pro.groundflip.domain.entity.redis.BlacklistedToken;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.BlackListedTokenRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

class JwtProviderTest {
	@InjectMocks
	private JwtProvider jwtProvider;
	@Mock
	private BlackListedTokenRepository blackListedTokenRepository;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(jwtProvider, "jwtSecretKey", "testSecretKey");
		ReflectionTestUtils.setField(jwtProvider, "accessTokenTime", 3600000L); // 1 hour
		ReflectionTestUtils.setField(jwtProvider, "refreshTokenTime", 86400000L); // 1 day
	}

	@Test
	@DisplayName("[createAccessToken] 엑세스 토큰을 생성한다")
	void createAccessTokenTest() {
		Long userId = 1L;
		String token = jwtProvider.createAccessToken(userId);
		System.out.println(token);
		assertNotNull(token);
	}

	@Test
	@DisplayName("[createRefreshToken] 리프레시 토큰을 생성한다")
	void createRefreshToken() {
		Long userId = 1L;
		String token = jwtProvider.createRefreshToken(userId);
		assertNotNull(token);
	}

	@Test
	@DisplayName("[validateToken] 토큰을 검증한다.")
	void validateToken_ValidToken() {
		Long userId = 1L;
		String token = jwtProvider.createAccessToken(userId);

		doReturn(false).when(blackListedTokenRepository).existsById(token);

		assertDoesNotThrow(() -> jwtProvider.validateToken(token));
	}

	@Test
	@DisplayName("[validateToken] 토큰이 없으면 AppException을 발생")
	void validateToken_NoToken() {
		String token = null;

		AppException exception = assertThrows(AppException.class, () -> jwtProvider.validateToken(token));
		assertEquals(exception.getErrorCode(), ErrorCode.JWT_NOT_EXISTS);
	}

	@Test
	@DisplayName("[validateToken] 잘못된 형식의 토큰은 AppException을 발생")
	void validateToken_InvalidToken() {
		String token = "invalidToken";

		AppException exception = assertThrows(AppException.class, () -> jwtProvider.validateToken(token));
		assertEquals(exception.getErrorCode(), ErrorCode.INVALID_JWT);
	}

	@Test
	@DisplayName("[validateToken] 만료된 토큰은 AppException을 발생")
	void validateToken_ExpiredToken() {
		String token = Jwts.builder()
			.setIssuedAt(new Date(System.currentTimeMillis() - 10000))
			.setExpiration(new Date(System.currentTimeMillis() - 5000))
			.signWith(SignatureAlgorithm.HS256, "testSecretKey")
			.compact();

		AppException exception = assertThrows(AppException.class, () -> jwtProvider.validateToken(token));
		assertEquals(exception.getErrorCode(), ErrorCode.JWT_EXPIRED);
	}

	@Test
	@DisplayName("[validateToken] 블랙리스트에 들어있는 토큰은 AppException을 발생")
	void validateToken_BlackListedToken() {
		Long userId = 1L;
		String token = jwtProvider.createAccessToken(userId);

		doReturn(true).when(blackListedTokenRepository).existsById(token);

		AppException exception = assertThrows(AppException.class, () -> jwtProvider.validateToken(token));
		assertEquals(exception.getErrorCode(), ErrorCode.INVALID_JWT);
	}

	@Test
	@DisplayName("[parseUserId] 토큰에서 userId를 파싱한다.")
	void parseUserIdTest() {
		Long userId = 1L;
		String token = jwtProvider.createAccessToken(userId);

		Long parsedUserId = jwtProvider.parseUserId(token);
		assertEquals(userId, parsedUserId);
	}

	@Test
	@DisplayName("[expireToken] 올바른 토큰이면 블랙리스트에 토큰을 집어 넣는다.")
	void expireToken_ValidToken() {
		Long userId = 1L;
		String token = jwtProvider.createAccessToken(userId);

		doReturn(false).when(blackListedTokenRepository).existsById(token);

		jwtProvider.expireToken(token);

		verify(blackListedTokenRepository, times(1)).save(any(BlacklistedToken.class));
	}

	@Test
	@DisplayName("[expireToken] 올바른 토큰이 아니면 블랙리스트에 넣지 않는다.")
	void expireToken_InvalidToken() {
		String token = "invalidToken";

		jwtProvider.expireToken(token);

		verify(blackListedTokenRepository, never()).save(any(BlacklistedToken.class));
	}

	@Test
	@DisplayName("[parseHeaders] jwt 토큰에서 header 부분을 파싱해온다.")
	public void parseHeadersTest() throws JsonProcessingException {
		String header = Base64.getEncoder().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(
			StandardCharsets.UTF_8));
		String token = header + ".payload.signature";

		Map<String, String> headers = jwtProvider.parseHeaders(token);

		assertNotNull(headers);
		assertEquals("HS256", headers.get("alg"));
		assertEquals("JWT", headers.get("typ"));
	}
}
