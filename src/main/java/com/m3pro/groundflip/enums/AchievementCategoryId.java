package com.m3pro.groundflip.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AchievementCategoryId {
	EXPLORER(1L),
	CONQUEROR(2L),
	RANKER(3L),
	STEADY(4L),
	SPECIAL(5L);

	private final Long categoryId;
}
