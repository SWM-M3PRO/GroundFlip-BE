package com.m3pro.groundflip.jwt;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.m3pro.groundflip.domain.entity.redis.BlacklistedToken;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.BlackListedTokenRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtProvider {
	private final BlackListedTokenRepository blackListedTokenRepository;
	@Value("${jwt.secret}")
	private String jwtSecretKey;
	@Value("${jwt.access-token-time}")
	private long accessTokenTime;
	@Value("${jwt.refresh-token-time}")
	private long refreshTokenTime;

	public String createAccessToken(Long userId) {
		return createToken(userId, accessTokenTime);
	}

	public String createRefreshToken(Long userId) {
		return createToken(userId, refreshTokenTime);
	}

	private String createToken(Long userId, long validTime) {
		Date now = new Date();
		return Jwts.builder()
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + validTime))
			.claim("userId", userId)
			.signWith(SignatureAlgorithm.HS256, jwtSecretKey)
			.compact();
	}

	public void validateToken(String token) {
		if (!StringUtils.hasText(token)) {
			throw new AppException(ErrorCode.JWT_NOT_EXISTS);
		}
		try {
			Jwts.parser().setSigningKey(jwtSecretKey).parseClaimsJws(token).getBody();
		} catch (SignatureException | MalformedJwtException e) {
			throw new AppException(ErrorCode.INVALID_JWT);
		} catch (ExpiredJwtException e) {
			throw new AppException(ErrorCode.JWT_EXPIRED);
		}
		if (isTokenBlackListed(token)) {
			throw new AppException(ErrorCode.INVALID_JWT);
		}
	}

	public Long parseUserId(String token) {
		Claims claims = Jwts.parser().setSigningKey(jwtSecretKey).parseClaimsJws(token).getBody();
		return claims.get("userId", Long.class);
	}

	private Long parseExpirationSecs(String token) {
		long expirationTimeMillis = Jwts.parser()
			.setSigningKey(jwtSecretKey)
			.parseClaimsJws(token)
			.getBody()
			.getExpiration()
			.getTime();
		return (expirationTimeMillis - System.currentTimeMillis()) / 1000;
	}

	private boolean isTokenBlackListed(String token) {
		return blackListedTokenRepository.existsById(token);
	}

	public void expireToken(String token) {
		try {
			validateToken(token);
			blackListedTokenRepository.save(new BlacklistedToken(token, parseExpirationSecs(token)));
		} catch (Exception ignored) {
		}
	}

	public Map<String, String> parseHeaders(String token) throws JsonProcessingException {
		String header = token.split("\\.")[0];
		return new ObjectMapper().readValue(decodeHeader(header), Map.class);
	}

	public String decodeHeader(String token) {
		return new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
	}

	public Claims getTokenClaims(String token, PublicKey publicKey) {
		try {
			return Jwts.parser()
				.setSigningKey(publicKey)
				.parseClaimsJws(token)
				.getBody();
		} catch (SignatureException | MalformedJwtException e) {
			throw new AppException(ErrorCode.INVALID_JWT);
		} catch (ExpiredJwtException e) {
			throw new AppException(ErrorCode.JWT_EXPIRED);
		}
	}
}

