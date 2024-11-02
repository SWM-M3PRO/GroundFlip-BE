package com.m3pro.groundflip.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.notification.NotificationResponse;
import com.m3pro.groundflip.domain.entity.UserNotification;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.UserNotificationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
	private final UserNotificationRepository userNotificationRepository;

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
}
