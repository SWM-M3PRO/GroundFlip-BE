package com.m3pro.groundflip.scheduler.service;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.entity.Achievement;
import com.m3pro.groundflip.enums.PushKind;
import com.m3pro.groundflip.enums.PushTarget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerNotificationService {
	private final SchedulerFcmService schedulerFcmService;
	private final SchedulerNotificationManager schedulerNotificationManager;

	public void createAchievementNotification(Long userId, Achievement achievement) {
		schedulerNotificationManager.createAchievementNotification(userId, achievement);
		schedulerFcmService.sendNotificationToUser(achievement.getName() + " 획득!", achievement.getName() + " 획득하였습니다!",
			userId);
	}

	public void createAnnouncementNotification(String title, String contents, String message) {
		schedulerNotificationManager.createAnnouncementNotification(title, contents);
		schedulerFcmService.sendNotificationToAllUsers(title, message, PushTarget.ALL, PushKind.SERVICE);
	}
}
