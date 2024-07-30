package com.m3pro.groundflip.domain.dto.auth;

import com.m3pro.groundflip.enums.Provider;

public interface OauthUserInfoResponse {
	String getEmail();

	Provider getOAuthProvider();
}

