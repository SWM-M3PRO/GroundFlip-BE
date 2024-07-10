package com.m3pro.groundflip.service.oauth;

import com.m3pro.groundflip.domain.dto.auth.KaKaoUserInfoResponse;
import com.m3pro.groundflip.domain.dto.auth.KakaoTokenValidationResponse;
import com.m3pro.groundflip.domain.dto.auth.OauthUserInfoResponse;
import com.m3pro.groundflip.enums.Provider;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class KakaoApiClient implements OauthApiClient {
    @Value("${oauth.kakao.url.user}")
    private String kakaoUserInfoUrl;

    @Value("${oauth.kakao.url.validation}")
    private String kakaoTokenValidationUrl;

    @Value("${oauth.kakao.app.id}")
    private Integer kakaoAppId;

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

    @Override
    public boolean isOauthTokenValid(String accessToken) {
        KakaoTokenValidationResponse validationResponse = restClient.get()
                .uri(kakaoTokenValidationUrl)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new AppException(ErrorCode.UNAUTHORIZED);
                })
                .body(KakaoTokenValidationResponse.class);

        if (validationResponse == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return validationResponse.getAppId().intValue() == kakaoAppId.intValue();
    }
}
