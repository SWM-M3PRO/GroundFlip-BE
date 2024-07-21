package com.m3pro.groundflip.service.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.m3pro.groundflip.domain.dto.auth.KaKaoUserInfoResponse;
import com.m3pro.groundflip.domain.dto.auth.KakaoTokenValidationResponse;
import com.m3pro.groundflip.domain.dto.auth.OauthUserInfoResponse;
import com.m3pro.groundflip.enums.Provider;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KakaoApiClient implements OauthApiClient {
	private final RestClient restClient;
	@Value("${oauth.kakao.url.user}")
	private String kakaoUserInfoUrl;
	@Value("${oauth.kakao.url.validation}")
	private String kakaoTokenValidationUrl;
	@Value("${oauth.kakao.app.id}")
	private Integer kakaoAppId;

	@Override
	public Provider oAuthProvider() {
		return Provider.KAKAO;
	}

	/**
	 * 카카오로부터 사용자 정보를 가져온다.
	 * @param accessToken 카카오의 액세스 토큰
	 * @return OauthUserInfoResponse 카카오에서 받아온 사용자의 정보
	 * @author 김민욱
	 */
	@Override
	public OauthUserInfoResponse requestOauthUserInfo(String accessToken) {
		return restClient.get()
			.uri(kakaoUserInfoUrl)
			.header("Authorization", "Bearer " + accessToken)
			.header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
			.retrieve()
			.body(KaKaoUserInfoResponse.class);
	}

	/**
	 * 카카오로부터 액세스 토큰을 검증한다..
	 * @param accessToken 카카오의 액세스 토큰
	 * @return boolean 토큰이 유효한지 여부
	 * @author 김민욱
	 */
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
