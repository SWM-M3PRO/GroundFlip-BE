package com.m3pro.groundflip.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Version {
	OK("업데이트 불필요"),
	NEED("업데이트 필요"),
	FORCE("강제 업데이트");

	private final String update;
}
