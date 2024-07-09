package com.m3pro.groundflip.service.oauth;

import com.m3pro.groundflip.domain.dto.auth.OauthUserInfoResponse;
import com.m3pro.groundflip.enums.Provider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RequestOauthUserInfoService {
    private final Map<Provider, OauthApiClient> clients;

    public RequestOauthUserInfoService(List<OauthApiClient> clients) {
        this.clients = clients.stream().collect(
                Collectors.toUnmodifiableMap(OauthApiClient::oAuthProvider, Function.identity())
        );
    }

    public OauthUserInfoResponse request(Provider provider, String accessToken) {
        OauthApiClient oauthApiClient = clients.get(provider);
        return oauthApiClient.requestOauthUserInfo(accessToken);
    }
}
