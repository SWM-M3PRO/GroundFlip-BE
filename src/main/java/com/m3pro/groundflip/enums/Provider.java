package com.m3pro.groundflip.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Provider {
	NAVER("네이버"),
	GOOGLE("구글"),
	APPLE("애플"),
	KAKAO("카카오");

	private final String provider;
}
