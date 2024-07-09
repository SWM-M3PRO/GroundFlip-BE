package com.m3pro.groundflip.service;

import com.m3pro.groundflip.domain.dto.auth.KaKaoUserInfoResponse;
import com.m3pro.groundflip.domain.dto.auth.KakaoLoginRequest;
import com.m3pro.groundflip.domain.dto.auth.LoginResponse;
import com.m3pro.groundflip.domain.dto.auth.OauthUserInfoResponse;
import com.m3pro.groundflip.enums.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final RequestOauthUserInfoService oauthUserInfoService;

    public LoginResponse loginKakao(KakaoLoginRequest kakaoLoginRequest) {
        OauthUserInfoResponse kaKaoUserInfo = oauthUserInfoService.request(Provider.KAKAO, kakaoLoginRequest.getAccessToken());
        System.out.println("kaKaoUserInfo = " + kaKaoUserInfo.getEmail());
        return new LoginResponse(kaKaoUserInfo.getEmail(), kaKaoUserInfo.getEmail(), false);
    }
}
