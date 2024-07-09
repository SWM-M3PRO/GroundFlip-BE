package com.m3pro.groundflip.service;

import com.m3pro.groundflip.domain.dto.auth.KaKaoUserInfoResponse;
import com.m3pro.groundflip.domain.dto.auth.OauthUserInfoResponse;
import com.m3pro.groundflip.enums.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class KakaoApiClient implements OauthApiClient {
    @Value("${oauth.kakao.url.user}")
    private String kakaoUserInfoUrl;

    private final RestClient restClient;

    @Override
    public Provider oAuthProvider() {
        return Provider.KAKAO;
    }

    @Override
    public OauthUserInfoResponse requestOauthUserInfo(String accessToken) {
        return restClient.get()
                .uri(kakaoUserInfoUrl)
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .body(KaKaoUserInfoResponse.class);
    }
}
