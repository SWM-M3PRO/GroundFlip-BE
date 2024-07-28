package com.m3pro.groundflip.domain.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class AppleTokenResponse {
	private String access_token;
	private String expires_in;
	private String id_token;
	private String refresh_token;
	private String token_type;
	private String error;
}
