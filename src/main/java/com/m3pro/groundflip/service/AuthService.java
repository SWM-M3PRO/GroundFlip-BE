package com.m3pro.groundflip.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.auth.LoginRequest;
import com.m3pro.groundflip.domain.dto.auth.LoginResponse;
import com.m3pro.groundflip.domain.dto.auth.LogoutRequest;
import com.m3pro.groundflip.domain.dto.auth.OauthUserInfoResponse;
import com.m3pro.groundflip.domain.dto.auth.ReissueReponse;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.enums.Provider;
import com.m3pro.groundflip.enums.UserStatus;
import com.m3pro.groundflip.jwt.JwtProvider;
import com.m3pro.groundflip.repository.RankingRedisRepository;
import com.m3pro.groundflip.repository.UserRepository;
import com.m3pro.groundflip.service.oauth.OauthService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final OauthService oauthUserInfoService;
	private final RankingRedisRepository rankingRedisRepository;
	private final JwtProvider jwtProvider;
	private final UserRepository userRepository;

	/**
	 * Oauth Provider를 사용해 로그인을 진행한다.
	 * @param provider oauth 프로바이더
	 * @param loginRequest 프로바이더의 액세스 토큰을 담는 객체
	 * @return LoginResponse 서버에서 발급한 액세스 토큰
	 * @author 김민욱
	 */
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

	/**
	 * 최초 접속일 경우 사용자를 등록한다.
	 * @param provider oauth 프로바이더
	 * @param email 프로바이더에서 제공하는 사용자의 이메일
	 * @return User 저장된 유저의 객체
	 * @author 김민욱
	 */
	private User registerUser(Provider provider, String email) {
		User newUser = User.builder()
			.email(email)
			.provider(provider)
			.status(UserStatus.PENDING)
			.build();
		return userRepository.save(newUser);
	}

	/**
	 * 토큰을 재발급 한다. 기존의 리프레시 토큰은 만료시키고 새로운 엑세스 토큰과 리프레시 토큰을 발급한다.
	 * @param refreshToken 리프레시 토큰
	 * @return 새로발급된 엑세스 토큰, 리프레시 토큰
	 */
	public ReissueReponse reissueToken(String refreshToken) {
		jwtProvider.validateToken(refreshToken);
		jwtProvider.expireToken(refreshToken);
		Long parsedUserId = jwtProvider.parseUserId(refreshToken);
		String reissuedAccessToken = jwtProvider.createAccessToken(parsedUserId);
		String reissuedRefreshToken = jwtProvider.createRefreshToken(parsedUserId);
		return new ReissueReponse(reissuedAccessToken, reissuedRefreshToken);
	}

	/**
	 * 로그아웃을 한다. 기존의 토큰을 블랙리스트에 넣어 만료시킨다.
	 * @param logoutRequest 엑세스토큰, 리프레시 토큰
	 */
	public void logout(LogoutRequest logoutRequest) {
		String accessToken = logoutRequest.getAccessToken();
		String refreshToken = logoutRequest.getRefreshToken();

		jwtProvider.expireToken(accessToken);
		jwtProvider.expireToken(refreshToken);
	}
}
