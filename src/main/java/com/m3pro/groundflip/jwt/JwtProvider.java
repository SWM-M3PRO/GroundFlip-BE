package com.m3pro.groundflip.jwt;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

@Component
public class JwtProvider {
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

	public boolean isTokenValid(String token) {
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
		return true;
	}

	public Long parseUserId(String token) {
		Claims claims = Jwts.parser().setSigningKey(jwtSecretKey).parseClaimsJws(token).getBody();
		return claims.get("userId", Long.class);
	}
}

