package com.m3pro.groundflip.domain.dto.achievement;

import java.time.LocalDateTime;

public interface AchievementElementInterface {
	Long getAchievementId();

	String getAchievementName();

	String getBadgeImageUrl();

	LocalDateTime getObtainedDate();

	Long getCategoryId();
}
