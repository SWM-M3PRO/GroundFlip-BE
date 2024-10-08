package com.m3pro.groundflip.service;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.permission.PermissionRequest;
import com.m3pro.groundflip.domain.dto.permission.PermissionResponse;
import com.m3pro.groundflip.domain.entity.Permission;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.PermissionRepository;
import com.m3pro.groundflip.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {
	private final UserRepository userRepository;
	private final PermissionRepository permissionRepository;

	public PermissionResponse getAllPermissions(Long userId) {
		User user = userRepository.getReferenceById(userId);
		Permission permission = permissionRepository.findByUser(user)
			.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		return new PermissionResponse(
			permission.getServiceNotificationsEnabled(),
			permission.getMarketingNotificationsEnabled()
		);
	}

	@Transactional
	public void updateServiceNotificationsPreference(PermissionRequest permissionRequest) {
		User user = userRepository.findById(permissionRequest.getUserId()).orElseThrow(() -> new AppException(
			ErrorCode.USER_NOT_FOUND));
		Permission permission = permissionRepository.findByUser(user)
			.orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR));
		permission.updateServiceNotificationsEnabled(permissionRequest.isEnabled());
	}

	@Transactional
	public void updateMarketingNotificationsPreference(PermissionRequest permissionRequest) {
		User user = userRepository.findById(permissionRequest.getUserId()).orElseThrow(() -> new AppException(
			ErrorCode.USER_NOT_FOUND));
		Permission permission = permissionRepository.findByUser(user)
			.orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR));
		permission.updateMarketingNotificationsEnabled(permissionRequest.isEnabled());
	}
}
