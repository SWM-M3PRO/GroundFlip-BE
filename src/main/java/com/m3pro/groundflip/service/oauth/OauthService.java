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

	public OauthUserInfoResponse requestUserInfo(Provider provider, String accessToken) {
		if (!isOauthTokenValid(provider, accessToken)) {
			throw new AppException(ErrorCode.UNAUTHORIZED);
		}
		OauthApiClient oauthApiClient = clients.get(provider);
		return oauthApiClient.requestOauthUserInfo(accessToken);
	}

	private boolean isOauthTokenValid(Provider provider, String accessToken) {
		OauthApiClient oauthApiClient = clients.get(provider);
		return oauthApiClient.isOauthTokenValid(accessToken);
	}
}
