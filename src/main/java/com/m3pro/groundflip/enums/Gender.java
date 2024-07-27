package com.m3pro.groundflip.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Gender {
	MALE("남성"),
	FEMALE("여성"),
	NONE("공개 안함");

	private final String gender;
}
