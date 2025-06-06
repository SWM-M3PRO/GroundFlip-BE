package com.m3pro.groundflip.scheduler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Ranking {
	TOP_100(100, 22L),
	TOP_50(50, 23L),
	TOP_30(30, 24L),
	TOP_10(10, 25L),
	TOP_3(3, 26L);

	private final int ranking;
	private final Long achievementId;
}
