package com.m3pro.groundflip.service;

import com.m3pro.groundflip.domain.dto.auth.KakaoLoginRequest;
import com.m3pro.groundflip.domain.dto.auth.LoginResponse;
import com.m3pro.groundflip.domain.dto.auth.OauthUserInfoResponse;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.enums.Provider;
import com.m3pro.groundflip.jwt.JwtProvider;
import com.m3pro.groundflip.repository.UserRepository;
import com.m3pro.groundflip.service.oauth.RequestOauthUserInfoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final RequestOauthUserInfoService oauthUserInfoService;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Transactional
    public LoginResponse loginKakao(KakaoLoginRequest kakaoLoginRequest) {
        Long userId;
        boolean isSignUp;

        OauthUserInfoResponse kaKaoUserInfo = oauthUserInfoService.request(Provider.KAKAO, kakaoLoginRequest.getAccessToken());
        Optional<User> loginUser = userRepository.findByProviderAndEmail(Provider.KAKAO, kaKaoUserInfo.getEmail());

        if (loginUser.isPresent()) {
            userId = loginUser.get().getId();
            isSignUp = false;
        } else {
            userId = registerKakaoUser(kaKaoUserInfo.getEmail()).getId();
            isSignUp = true;
        }

        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId);
        return new LoginResponse(accessToken, refreshToken, isSignUp);
    }

    private User registerKakaoUser(String email) {
        User registerUser = User.builder()
                .email(email)
                .provider(Provider.KAKAO)
                .build();
        return userRepository.save(registerUser);
    }
}
