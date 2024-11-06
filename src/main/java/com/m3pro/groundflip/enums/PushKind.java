package com.m3pro.groundflip.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PushKind {
	SERVICE("service"),
	MARKETING("marketing");

	private final String kind;
}
