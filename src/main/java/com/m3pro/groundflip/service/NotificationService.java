package com.m3pro.groundflip.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.notification.NotificationResponse;
import com.m3pro.groundflip.domain.entity.Achievement;
import com.m3pro.groundflip.domain.entity.Notification;
import com.m3pro.groundflip.domain.entity.UserNotification;
import com.m3pro.groundflip.enums.NotificationCategory;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.NotificationRepository;
import com.m3pro.groundflip.repository.UserNotificationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
	private final UserNotificationRepository userNotificationRepository;
	private final NotificationRepository notificationRepository;
	private final FcmService fcmService;

	public List<NotificationResponse> getAllNotifications(Long userId) {
		LocalDateTime lookupDate = LocalDateTime.now().minusDays(14);
		List<UserNotification> userNotifications = userNotificationRepository.findAllByUserId(userId, lookupDate);

		return userNotifications.stream().map((NotificationResponse::from)).toList();
	}

	@Transactional
	public void markNotificationAsRead(Long notificationId) {
		UserNotification userNotification = userNotificationRepository.findById(notificationId)
			.orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

		userNotification.markAsRead();
	}

	public boolean checkForUnreadNotifications(Long userId) {
		LocalDateTime lookupDate = LocalDateTime.now().minusDays(14);
		return userNotificationRepository.existsByUserId(userId, lookupDate);
	}

	@Transactional
	public void createAchievementNotification(Long userId, Achievement achievement) {
		Notification notification = notificationRepository.save(Notification.builder()
			.title(achievement.getName() + " 획득!")
			.category(NotificationCategory.ACHIEVEMENT.getCategoryName())
			.categoryId(NotificationCategory.ACHIEVEMENT.getCategoryId())
			.contentId(achievement.getId())
			.build()
		);
		userNotificationRepository.save(UserNotification.builder()
			.userId(userId)
			.notification(notification)
			.build());
		fcmService.sendNotificationToUser(achievement.getName() + " 획득!", achievement.getName() + " 획득하였습니다!", userId);
	}
}
