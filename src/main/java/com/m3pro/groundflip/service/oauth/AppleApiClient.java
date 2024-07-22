package com.m3pro.groundflip.service.oauth;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.m3pro.groundflip.domain.dto.auth.AppleUserInfoResponse;
import com.m3pro.groundflip.domain.dto.auth.OauthUserInfoResponse;
import com.m3pro.groundflip.enums.Provider;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.jwt.JwtProvider;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppleApiClient implements OauthApiClient {
	private final ApplePublicKeyGenerator applePublicKeyGenerator;
	private final JwtProvider jwtProvider;
	@Value("${oauth.apple.url.issuer}")
	private String issuer;
	@Value("${oauth.apple.app.id}")
	private String clientId;

	@Override
	public Provider oAuthProvider() {
		return Provider.APPLE;
	}

	/**
	 * identityToken 으로부터 이메일을 얻어온다.
	 * @param identityToken identityToken
	 * @return 이메일, provider 정보
	 */
	@Override
	public OauthUserInfoResponse requestOauthUserInfo(String identityToken) {
		String email = jwtProvider.parsePayLoad(identityToken).get("email");
		return new AppleUserInfoResponse(email);
	}

	/**
	 * identity token 이 유효한 토큰인지 검사한다.
	 * @param identityToken identityToken
	 * @return true, false
	 */
	@Override
	public boolean isOauthTokenValid(String identityToken) {
		try {
			verifyIdentityToken(identityToken);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * IdentityToken을 검사한다.
	 * @param identityToken
	 * @throws JsonProcessingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	private void verifyIdentityToken(String identityToken) throws
		JsonProcessingException,
		NoSuchAlgorithmException,
		InvalidKeySpecException {
		Map<String, String> headers = jwtProvider.parseHeaders(identityToken);
		PublicKey publicKey = applePublicKeyGenerator.getPublicKey(headers);
		Claims tokenClaims = jwtProvider.validateTokenWithPublicKey(identityToken, publicKey);

		if (!issuer.equals(tokenClaims.getIssuer())) {
			throw new AppException(ErrorCode.INVALID_JWT);
		}
		if (!clientId.equals(tokenClaims.getAudience())) {
			throw new AppException(ErrorCode.INVALID_JWT);
		}
	}
}
