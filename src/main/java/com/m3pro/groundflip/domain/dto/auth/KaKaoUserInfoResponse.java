package com.m3pro.groundflip.domain.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.m3pro.groundflip.enums.Provider;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)

public class KaKaoUserInfoResponse implements OauthUserInfoResponse {
	@JsonProperty("kakao_account")
	private KakaoAccount kakaoAccount;

	@Override
	public String getEmail() {
		return kakaoAccount.getEmail();
	}

	@Override
	public Provider getOAuthProvider() {
		return Provider.KAKAO;
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	static class KakaoAccount {
		private String email;
	}
}

