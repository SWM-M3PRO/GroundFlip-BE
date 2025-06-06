package com.m3pro.groundflip.scheduler.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.m3pro.groundflip.domain.entity.FcmToken;
import com.m3pro.groundflip.enums.PushKind;
import com.m3pro.groundflip.enums.PushTarget;
import com.m3pro.groundflip.scheduler.repository.SchedulerFcmTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerFcmService {
	private static final int BATCH_SIZE = 100;
	private final FirebaseMessaging firebaseMessaging;
	private final SchedulerFcmTokenRepository schedulerFcmTokenRepository;
	private final SchedulerFcmTokenService schedulerFcmTokenService;

	private static Notification createNotification(String title, String body) {
		return Notification.builder()
			.setTitle(title)
			.setBody(body)
			.build();
	}

	public void sendNotificationToAllUsers(String title, String body, PushTarget target, PushKind kind) {
		List<FcmToken> fcmTokens = schedulerFcmTokenService.findFcmTokens(target, kind);

		sendNotificationToUsers(title, body, fcmTokens);
	}

	private void sendNotificationToUsers(String title, String body, List<FcmToken> fcmTokens) {
		log.info("[sendNotificationToUsers] 전체 유저에게 푸시 알림 발송 시작");
		List<List<String>> batches = splitIntoBatches(fcmTokens);
		List<String> invalidTokens = new ArrayList<>();

		for (List<String> batch : batches) {
			MulticastMessage multicastMessage = createMulticastMessage(title, body, batch);

			try {
				BatchResponse batchResponse = firebaseMessaging.sendEachForMulticast(multicastMessage);
				invalidTokens.addAll(extractInvalidTokens(batch, batchResponse));
			} catch (FirebaseMessagingException e) {
				log.error("{} {}", e.getMessage(), e.getStackTrace());
			}
		}
		schedulerFcmTokenService.removeAllTokens(invalidTokens);
		log.info("[sendNotificationToUsers] 전체 유저에게 푸시 알림 발송 완료, {} 개의 토큰 발송 실패", invalidTokens.size());
	}

	private List<String> extractInvalidTokens(List<String> tokens, BatchResponse batchResponse) {
		List<SendResponse> responses = batchResponse.getResponses();
		List<String> invalidTokens = new ArrayList<>();

		for (int i = 0; i < responses.size(); i++) {
			SendResponse response = responses.get(i);
			if (!response.isSuccessful() && isInvalidTokenError(response.getException())) {
				invalidTokens.add(tokens.get(i));
			}
		}
		return invalidTokens;
	}

	private MulticastMessage createMulticastMessage(String title, String body, List<String> batch) {
		return MulticastMessage.builder()
			.setNotification(createNotification(title, body))
			.setApnsConfig(getApnsConfig())
			.addAllTokens(batch)
			.build();
	}

	private List<List<String>> splitIntoBatches(List<FcmToken> fcmTokens) {
		List<List<String>> batches = new ArrayList<>();
		List<String> currentBatch = new ArrayList<>();

		for (FcmToken fcmToken : fcmTokens) {
			currentBatch.add(fcmToken.getToken());
			if (currentBatch.size() == BATCH_SIZE) {
				batches.add(currentBatch);
				currentBatch = new ArrayList<>();
			}
		}
		if (!currentBatch.isEmpty()) {
			batches.add(currentBatch);
		}
		return batches;
	}

	public void sendNotificationToUser(String title, String body, Long userId) {
		Optional<FcmToken> fcmToken = schedulerFcmTokenRepository.findTokenForServiceNotifications(userId);
		if (fcmToken.isPresent()) {
			FcmToken token = fcmToken.get();
			try {
				log.info("success send notification to user [{}]: tokenId = {}", token.getUser().getId(),
					token.getId());
				sendMessage(title, body, token.getToken());
			} catch (FirebaseMessagingException e) {
				if (isInvalidTokenError(e)) {
					schedulerFcmTokenRepository.deleteByUser(token.getUser());
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
