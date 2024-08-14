package com.m3pro.groundflip.controller;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.permission.PermissionRequest;
import com.m3pro.groundflip.service.PermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/permission")
@Tag(name = "permission", description = "권한 동의 API")
@SecurityRequirement(name = "Authorization")
public class PermissionController {
	private final PermissionService permissionService;

	@Operation(summary = "서비스 알림 권한 동의", description = "서비스 푸시 알림을 받을지 받지 않을지 선택하는 api 이다.")
	@PutMapping("/service-notification")
	public Response<?> updateServiceNotification(@RequestBody PermissionRequest permissionRequest) {
		permissionService.updateServiceNotificationsPreference(permissionRequest);
		return Response.createSuccessWithNoData();
	}

	@Operation(summary = "마케팅 알림 권한 동의", description = "마케팅 푸시 알림을 받을지 받지 않을지 선택하는 api 이다.")
	@PutMapping("/marketing-notification")
	public Response<?> updateMarketingNotification(@RequestBody PermissionRequest permissionRequest) {
		permissionService.updateMarketingNotificationsPreference(permissionRequest);
		return Response.createSuccessWithNoData();
	}
}
