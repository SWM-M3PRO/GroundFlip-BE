package com.m3pro.groundflip.service.oauth;

import com.m3pro.groundflip.domain.dto.auth.OauthUserInfoResponse;
import com.m3pro.groundflip.enums.Provider;

public interface OauthApiClient {
    Provider oAuthProvider();
    OauthUserInfoResponse requestOauthUserInfo(String accessToken);
    boolean isOauthTokenValid(String accessToken);
}
