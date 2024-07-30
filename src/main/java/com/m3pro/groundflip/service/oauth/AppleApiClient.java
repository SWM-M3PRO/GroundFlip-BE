package com.m3pro.groundflip.service.oauth;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.m3pro.groundflip.domain.dto.auth.AppleTokenResponse;
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
	private final AppleKeyGenerator appleKeyGenerator;
	private final RestClient restClient;
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
		PublicKey publicKey = appleKeyGenerator.getPublicKey(headers);
		Claims tokenClaims = jwtProvider.validateTokenWithPublicKey(identityToken, publicKey);

		if (!issuer.equals(tokenClaims.getIssuer())) {
			throw new AppException(ErrorCode.INVALID_JWT);
		}
		if (!clientId.equals(tokenClaims.getAudience())) {
			throw new AppException(ErrorCode.INVALID_JWT);
		}
	}

	/**
	 * apple 서버에서 발행하는 refresh token을 반환한다
	 * @param authorizationCode
	 * @return apple 에서 발행하는 refresh token
	 * @throws IOException
	 */
	public String getAppleRefreshToken(String authorizationCode) {
		MultiValueMap<String, String> body = getCreateTokenBody(authorizationCode);

		AppleTokenResponse appleTokenResponse = restClient.post()
			.uri("https://appleid.apple.com/auth/token")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(body)
			.retrieve()
			.body(AppleTokenResponse.class);

		return Objects.requireNonNull(appleTokenResponse).getRefresh_token();
	}

	private MultiValueMap<String, String> getCreateTokenBody(String authorizationCode) {
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("code", authorizationCode);
		body.add("client_id", clientId);
		body.add("client_secret", appleKeyGenerator.getClientSecret());
		body.add("grant_type", "authorization_code");
		return body;
	}

	public void revokeToken(String refreshToken) {
		MultiValueMap<String, String> body = getRevokeTokenBody(refreshToken);

		restClient.post()
			.uri("https://appleid.apple.com/auth/revoke")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(body)
			.retrieve()
			.toBodilessEntity();
	}

	private MultiValueMap<String, String> getRevokeTokenBody(String refreshToken) {
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("client_id", clientId);
		body.add("token", refreshToken);
		body.add("client_secret", appleKeyGenerator.getClientSecret());
		body.add("token_type_hint", "refresh_token");
		return body;
	}
}
