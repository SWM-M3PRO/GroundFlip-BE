package com.m3pro.groundflip.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

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
}
