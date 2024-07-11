package com.m3pro.groundflip.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.auth.LoginRequest;
import com.m3pro.groundflip.domain.dto.auth.LoginResponse;
import com.m3pro.groundflip.domain.dto.auth.OauthUserInfoResponse;
import com.m3pro.groundflip.domain.dto.auth.Tokens;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.enums.Provider;
import com.m3pro.groundflip.enums.UserStatus;
import com.m3pro.groundflip.jwt.JwtProvider;
import com.m3pro.groundflip.repository.UserRepository;
import com.m3pro.groundflip.service.oauth.OauthService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final OauthService oauthUserInfoService;
	private final JwtProvider jwtProvider;
	private final UserRepository userRepository;

	@Transactional
	public LoginResponse login(Provider provider, LoginRequest loginRequest) {
		Long userId;
		boolean isSignUp;

		OauthUserInfoResponse oauthUserInfo = oauthUserInfoService.requestUserInfo(provider,
			loginRequest.getAccessToken());
		Optional<User> loginUser = userRepository.findByProviderAndEmail(provider, oauthUserInfo.getEmail());

		if (loginUser.isPresent()) {
			userId = loginUser.get().getId();
			isSignUp = loginUser.get().getStatus() == UserStatus.PENDING;
		} else {
			userId = registerUser(provider, oauthUserInfo.getEmail()).getId();
			isSignUp = true;
		}

		String accessToken = jwtProvider.createAccessToken(userId);
		String refreshToken = jwtProvider.createRefreshToken(userId);
		return new LoginResponse(accessToken, refreshToken, isSignUp);
	}

	private User registerUser(Provider provider, String email) {
		User registerUser = User.builder()
			.email(email)
			.provider(provider)
			.status(UserStatus.PENDING)
			.build();
		return userRepository.save(registerUser);
	}

	public Tokens reissueToken(String refreshToken) {
		jwtProvider.isTokenValid(refreshToken);
		Long parsedUserId = jwtProvider.parseUserId(refreshToken);
		String reissuedAccessToken = jwtProvider.createAccessToken(parsedUserId);
		String reissuedRefreshToken = jwtProvider.createRefreshToken(parsedUserId);
		return new Tokens(reissuedAccessToken, reissuedRefreshToken);
	}
}
