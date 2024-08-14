package com.m3pro.groundflip.service;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.preference.PreferenceRequest;
import com.m3pro.groundflip.domain.entity.Preference;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.PreferenceRepository;
import com.m3pro.groundflip.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreferenceService {
	private final UserRepository userRepository;
	private final PreferenceRepository preferenceRepository;

	@Transactional
	public void updateServiceNotificationsPreference(PreferenceRequest preferenceRequest) {
		User user = userRepository.findById(preferenceRequest.getUserId()).orElseThrow(() -> new AppException(
			ErrorCode.USER_NOT_FOUND));
		Preference preference = preferenceRepository.findByUser(user)
			.orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR));
		preference.updateServiceNotificationsEnabled(preferenceRequest.isEnabled());
	}

	@Transactional
	public void updateMarketingNotificationsPreference(PreferenceRequest preferenceRequest) {
		User user = userRepository.findById(preferenceRequest.getUserId()).orElseThrow(() -> new AppException(
			ErrorCode.USER_NOT_FOUND));
		Preference preference = preferenceRepository.findByUser(user)
			.orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR));
		preference.updateMarketingNotificationsEnabled(preferenceRequest.isEnabled());
	}
}
