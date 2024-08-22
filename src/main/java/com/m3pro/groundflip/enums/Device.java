package com.m3pro.groundflip.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Device {
	IOS("iOS"),
	ANDROID("Android");

	private final String device;
}
