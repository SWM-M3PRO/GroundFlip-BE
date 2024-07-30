package com.m3pro.groundflip.service.oauth;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.m3pro.groundflip.domain.dto.auth.OauthUserInfoResponse;
import com.m3pro.groundflip.enums.Provider;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;

@Component
public class OauthService {
	private final Map<Provider, OauthApiClient> clients;

	public OauthService(List<OauthApiClient> clients) {
		this.clients = clients.stream().collect(
			Collectors.toUnmodifiableMap(OauthApiClient::oAuthProvider, Function.identity())
		);
	}

	/**
	 * Oauth Provider로부터 사용자 정보를 가져온다.
	 * @param provider oauth 프로바이더
	 * @param accessToken 카카오의 액세스 토큰
	 * @return OauthUserInfoResponse 프로바이더에서 받아온 사용자의 정보
	 * @author 김민욱
	 */
	public OauthUserInfoResponse requestUserInfo(Provider provider, String accessToken) {
		if (!isOauthTokenValid(provider, accessToken)) {
			throw new AppException(ErrorCode.UNAUTHORIZED);
		}
		OauthApiClient oauthApiClient = clients.get(provider);
		return oauthApiClient.requestOauthUserInfo(accessToken);
	}

	/**
	 * Oauth Provider로부터 토큰을 검증한다.
	 * @param provider oauth 프로바이더
	 * @param accessToken 프로바이더의 액세스 토큰
	 * @return boolean 토큰이 유효한지 여부
	 * @author 김민욱
	 */
	private boolean isOauthTokenValid(Provider provider, String accessToken) {
		OauthApiClient oauthApiClient = clients.get(provider);
		return oauthApiClient.isOauthTokenValid(accessToken);
	}

	public String getAppleRefreshToken(String authorizationCode) {
		AppleApiClient appleApiClient = (AppleApiClient)clients.get(Provider.APPLE);
		return appleApiClient.getAppleRefreshToken(authorizationCode);
	}
}
