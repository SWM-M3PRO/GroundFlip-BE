package com.m3pro.groundflip.service.oauth;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.m3pro.groundflip.domain.dto.auth.AppleUserInfoResponse;
import com.m3pro.groundflip.domain.dto.auth.OauthUserInfoResponse;
import com.m3pro.groundflip.domain.dto.auth.apple.ApplePublicKeyResponse;
import com.m3pro.groundflip.enums.Provider;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.jwt.JwtProvider;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppleApiClient implements OauthApiClient {
	private final RestClient restClient;
	private final ApplePublicKeyGenerator applePublicKeyGenerator;
	private final JwtProvider jwtProvider;
	@Value("${oauth.apple.url.public-keys}")
	private String applePublicKeysUrl;
	@Value("${oauth.apple.url.issuer}")
	private String issuer;
	@Value("${oauth.apple.app.id}")
	private String clientId;

	@Override
	public Provider oAuthProvider() {
		return Provider.APPLE;
	}

	@Override
	public OauthUserInfoResponse requestOauthUserInfo(String identityToken) {
		try {
			Map<String, String> headers = jwtProvider.parseHeaders(identityToken);
			PublicKey publicKey = applePublicKeyGenerator.generatePublicKey(headers, getAppleAuthPublicKey());
			String email = jwtProvider.getTokenClaims(identityToken, publicKey).get("email", String.class);
			return new AppleUserInfoResponse(email);
		} catch (Exception e) {
			throw new AppException(ErrorCode.UNAUTHORIZED);
		}
	}

	@Override
	public boolean isOauthTokenValid(String identityToken) {
		try {
			verifyIdentityToken(identityToken);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private ApplePublicKeyResponse getAppleAuthPublicKey() {
		return restClient.get()
			.uri(applePublicKeysUrl)
			.retrieve()
			.body(ApplePublicKeyResponse.class);
	}

	private void verifyIdentityToken(String identityToken) throws
		JsonProcessingException,
		NoSuchAlgorithmException,
		InvalidKeySpecException {
		Map<String, String> headers = jwtProvider.parseHeaders(identityToken);
		PublicKey publicKey = applePublicKeyGenerator.generatePublicKey(headers, getAppleAuthPublicKey());
		Claims tokenClaims = jwtProvider.getTokenClaims(identityToken, publicKey);

		if (!issuer.equals(tokenClaims.getIssuer())) {
			throw new AppException(ErrorCode.INVALID_JWT);
		}
		if (!clientId.equals(tokenClaims.getAudience())) {
			throw new AppException(ErrorCode.INVALID_JWT);
		}
	}
}
