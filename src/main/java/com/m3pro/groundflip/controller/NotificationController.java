package com.m3pro.groundflip.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.notification.NotificationResponse;
import com.m3pro.groundflip.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/notifications")
@Tag(name = "achievements", description = "알림 API")
@SecurityRequirement(name = "Authorization")
public class NotificationController {
	private final NotificationService notificationService;

	@Operation(summary = "유저 별 알림 목록 조회", description = "특정 유저의 14일 전부터 모든 알림 목록을 반환한다.")
	@Parameters({
		@Parameter(name = "user-id", description = "찾고자 하는 user의 id", example = "14"),
	})
	@GetMapping("")
	public Response<List<NotificationResponse>> getNotifications(
		@RequestParam(name = "user-id") Long userId
	) {
		return Response.createSuccess(notificationService.getAllNotifications(userId));
	}

	@Operation(summary = "알림 읽음 상태로 변경", description = "특정 알림의 상태를 읽음 상태로 변경한다.")
	@PatchMapping("/{notificationId}/read")
	public Response<?> markNotificationAsRead(@PathVariable("notificationId") Long notificationId) {
		notificationService.markNotificationAsRead(notificationId);
		return Response.createSuccessWithNoData();
	}
}
