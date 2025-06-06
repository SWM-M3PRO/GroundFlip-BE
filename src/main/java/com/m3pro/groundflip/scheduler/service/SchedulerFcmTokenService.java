package com.m3pro.groundflip.scheduler.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.entity.FcmToken;
import com.m3pro.groundflip.enums.PushKind;
import com.m3pro.groundflip.enums.PushTarget;
import com.m3pro.groundflip.scheduler.repository.SchedulerFcmTokenRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerFcmTokenService {
	private final SchedulerFcmTokenRepository schedulerFcmTokenRepository;

	public List<FcmToken> findFcmTokens(PushTarget target, PushKind kind) {
		List<FcmToken> fcmTokens;
		if (kind == PushKind.SERVICE) {
			if (target == PushTarget.ALL) {
				fcmTokens = schedulerFcmTokenRepository.findAllTokensForServiceNotifications();
			} else if (target == PushTarget.ANDROID) {
				fcmTokens = schedulerFcmTokenRepository.findAllAndroidTokensForServiceNotification();
			} else {
				fcmTokens = schedulerFcmTokenRepository.findAllIOSTokensForServiceNotification();
			}
		} else {
			if (target == PushTarget.ALL) {
				fcmTokens = schedulerFcmTokenRepository.findAllTokensForMarketingNotifications();
			} else if (target == PushTarget.ANDROID) {
				fcmTokens = schedulerFcmTokenRepository.findAllAndroidTokensForMarketingNotification();
			} else {
				fcmTokens = schedulerFcmTokenRepository.findAllIOSTokensForMarketingNotification();
			}
		}

		return fcmTokens;
	}

	@Transactional
	public void removeAllTokens(List<String> tokens) {
		schedulerFcmTokenRepository.deleteByTokenIn(tokens);
	}
}
