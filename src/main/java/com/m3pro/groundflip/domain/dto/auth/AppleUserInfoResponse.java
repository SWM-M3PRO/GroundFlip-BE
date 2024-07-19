package com.m3pro.groundflip.domain.dto.auth;

import com.m3pro.groundflip.enums.Provider;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AppleUserInfoResponse implements OauthUserInfoResponse {
	private String email;

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public Provider getOAuthProvider() {
		return Provider.APPLE;
	}
}
