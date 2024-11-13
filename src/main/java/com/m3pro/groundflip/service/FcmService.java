package com.m3pro.groundflip.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.m3pro.groundflip.domain.dto.user.FcmTokenRequest;
import com.m3pro.groundflip.domain.entity.FcmToken;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.domain.entity.UserActivityLog;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.FcmTokenRepository;
import com.m3pro.groundflip.repository.UserActivityLogRepository;
import com.m3pro.groundflip.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {
	private final UserRepository userRepository;
	private final FcmTokenRepository fcmTokenRepository;
	private final UserActivityLogRepository userActivityLogRepository;
	private final FirebaseMessaging firebaseMessaging;

	private static Notification createNotification(String title, String body) {
		return Notification.builder()
			.setTitle(title)
			.setBody(body)
			.build();
	}

	@Transactional
	public void registerFcmToken(FcmTokenRequest fcmTokenRequest) {
		Long userId = fcmTokenRequest.getUserId();
		User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		Optional<FcmToken> fcmToken = fcmTokenRepository.findByUser(user);
		logUserActivity(userId);

		if (fcmToken.isPresent()) {
			fcmToken.get().updateModifiedAtToNow();
			fcmToken.get().updateToken(fcmTokenRequest.getFcmToken());
			fcmToken.get().updateDevice(fcmTokenRequest.getDevice());
		} else {
			fcmTokenRepository.save(
				FcmToken.builder()
					.user(user)
					.token(fcmTokenRequest.getFcmToken())
					.device(fcmTokenRequest.getDevice())
					.build()
			);
		}
	}

	private void logUserActivity(Long userId) {
		UserActivityLog userActivityLog = UserActivityLog.builder()
			.userId(userId)
			.activity("APP_OPEN")
			.build();
		userActivityLogRepository.save(userActivityLog);
	}

	public void sendNotificationToUser(String title, String body, Long userId) {
		Optional<FcmToken> fcmToken = fcmTokenRepository.findTokenForServiceNotifications(userId);
		if (fcmToken.isPresent()) {
			FcmToken token = fcmToken.get();
			try {
				log.info("success send notification to user [{}]: tokenId = {}", token.getUser().getId(),
					token.getId());
				sendMessage(title, body, token.getToken());
			} catch (FirebaseMessagingException e) {
				if (isInvalidTokenError(e)) {
					fcmTokenRepository.deleteByUser(token.getUser());
					log.info("Failed to send notification to [{}]: {}", token.getUser().getId(), token.getToken());
				}
			}
		}
	}

	public void sendMessage(String title, String body, String fcmToken) throws FirebaseMessagingException {
		Message message = createMessage(title, body, fcmToken);
		firebaseMessaging.send(message);
	}

	private Message createMessage(String title, String body, String fcmToken) {
		Notification notification = createNotification(title, body);
		return Message.builder()
			.setToken(fcmToken)
			.setNotification(notification)
			.setApnsConfig(getApnsConfig())
			.build();
	}

	private ApnsConfig getApnsConfig() {
		return ApnsConfig.builder()
			.setAps(Aps.builder()
				.setSound("default")
				.build()
			).build();
	}

	private boolean isInvalidTokenError(FirebaseMessagingException e) {
		MessagingErrorCode errorCode = e.getMessagingErrorCode();
		return errorCode.equals(MessagingErrorCode.UNREGISTERED) || errorCode.equals(
			MessagingErrorCode.INVALID_ARGUMENT);
	}
}
