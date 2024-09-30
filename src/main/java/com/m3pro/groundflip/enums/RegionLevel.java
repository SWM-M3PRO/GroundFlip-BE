package com.m3pro.groundflip.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RegionLevel {
	CITY("city"),
	PROVINCE("PROVINCE");

	private final String level;
}
