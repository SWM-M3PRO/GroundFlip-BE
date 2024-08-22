package com.m3pro.groundflip.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Place {
	HOME("집"),
	COMPANY("회사/학교"),
	ELSE("기타 장소");

	private final String place;
}
