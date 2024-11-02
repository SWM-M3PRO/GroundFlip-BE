package com.m3pro.groundflip.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NotificationCategory {
	ANNOUNCEMENT(1L),
	NOTIFICATION(2L),
	ACHIEVEMENT(3L);

	private final Long categoryId;
}
