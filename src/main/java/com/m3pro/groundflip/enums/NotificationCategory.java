package com.m3pro.groundflip.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NotificationCategory {
	ANNOUNCEMENT(1L, "공지"),
	NOTIFICATION(2L, "알림"),
	ACHIEVEMENT(3L, "업적");

	private final Long categoryId;
	private final String categoryName;
}
