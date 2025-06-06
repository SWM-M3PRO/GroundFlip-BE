package com.m3pro.groundflip.scheduler.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.entity.Achievement;
import com.m3pro.groundflip.domain.entity.Notification;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.domain.entity.UserNotification;
import com.m3pro.groundflip.enums.NotificationCategory;
import com.m3pro.groundflip.scheduler.repository.SchedulerNotificationRepository;
import com.m3pro.groundflip.scheduler.repository.SchedulerUserNotificationRepository;
import com.m3pro.groundflip.scheduler.repository.SchedulerUserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerNotificationManager {
	private final SchedulerUserNotificationRepository schedulerUserNotificationRepository;
	private final SchedulerNotificationRepository schedulerNotificationRepository;
	private final SchedulerUserRepository schedulerUserRepository;
	private final SchedulerAnnouncementService schedulerAnnouncementService;

	@Transactional
	public void createAchievementNotification(Long userId, Achievement achievement) {
		Notification notification = createNotification(achievement.getName() + " 획득!", NotificationCategory.ACHIEVEMENT,
			achievement.getId());
		schedulerUserNotificationRepository.save(UserNotification.builder()
			.userId(userId)
			.notification(notification)
			.build());
	}

	@Transactional
	public void createAnnouncementNotification(String title, String contents) {
		Long announcementId = schedulerAnnouncementService.createAnnouncement(title, contents);
		Notification notification = createNotification(title, NotificationCategory.ANNOUNCEMENT, announcementId);
		createNotificationToAllUsers(notification);
	}

	private Notification createNotification(String title, NotificationCategory category, Long contentId) {
		return schedulerNotificationRepository.save(Notification.builder()
			.title(title)
			.category(category.getCategoryName())
			.categoryId(category.getCategoryId())
			.contentId(contentId)
			.build()
		);
	}

	private void createNotificationToAllUsers(Notification notification) {
		int page = 0;
		int pageSize = 500;

		Pageable pageable = PageRequest.of(page, pageSize);
		Page<User> userPage;
		do {
			userPage = schedulerUserRepository.findAll(pageable);
			List<UserNotification> userNotifications = userPage.getContent().stream()
				.map(user -> UserNotification.builder()
					.notification(notification)
					.userId(user.getId())
					.build())
				.collect(Collectors.toList());

			schedulerUserNotificationRepository.saveAll(userNotifications);
			schedulerUserNotificationRepository.flush(); // 캐시 초기화

			pageable = pageable.next(); // 다음 페이지로 이동
		} while (userPage.hasNext());
	}

}
