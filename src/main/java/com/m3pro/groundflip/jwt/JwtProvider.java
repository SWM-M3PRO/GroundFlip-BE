package com.m3pro.groundflip.jwt;

import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

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
                .signWith(SignatureAlgorithm.HS512, jwtSecretKey)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validTime))
                .claim("userId", userId)
                .compact();
    }

    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new AppException(ErrorCode.JWT_NOT_EXISTS);
        }
        try {
            Jwts.parser().setSigningKey(jwtSecretKey).parseClaimsJws(token).getBody();
        } catch (SignatureException | MalformedJwtException e) {
            throw new AppException(ErrorCode.INVALID_JWT);
        } catch (ExpiredJwtException e ) {
            throw new AppException(ErrorCode.JWT_EXPIRED);
        }
        return true;
    }

    public Long parseUserId(String token) {
        Claims claims = Jwts.parser().setSigningKey(jwtSecretKey).parseClaimsJws(token).getBody();
        return claims.get("userId", Long.class);
    }
}
