package com.m3pro.groundflip.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.user.FcmTokenRequest;
import com.m3pro.groundflip.domain.dto.user.UserDeleteRequest;
import com.m3pro.groundflip.domain.dto.user.UserInfoRequest;
import com.m3pro.groundflip.domain.dto.user.UserInfoResponse;
import com.m3pro.groundflip.service.FcmService;
import com.m3pro.groundflip.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "users", description = "사용자 API")
@SecurityRequirement(name = "Authorization")
public class UserController {
	private final UserService userService;
	private final FcmService fcmService;

	@Operation(summary = "사용자 기본 정보 조회", description = "닉네임, id, 출생년도, 성별, 프로필 사진, 그룹이름, 그룹 id 를 조회 한다.")
	@GetMapping("/{userId}")
	public Response<UserInfoResponse> getUserInfo(
		@Parameter(description = "찾고자 하는 userId", required = true)
		@PathVariable("userId") Long userId
	) {
		return Response.createSuccess(userService.getUserInfo(userId));
	}

	@Operation(summary = "사용자 정보 수정", description = "닉네임, id, 출생년도, 성별, 프로필 사진을 수정한다.")
	@PutMapping("/{userId}")
	public Response<?> putUserInfo(
		@Parameter(description = "찾고자 하는 userId", required = true)
		@PathVariable Long userId,
		@RequestPart UserInfoRequest userInfoRequest,
		@RequestPart(value = "profileImage", required = false) MultipartFile multipartfile
	) throws IOException {
		userService.putUserInfo(userId, userInfoRequest, multipartfile);
		return Response.createSuccessWithNoData();
	}

	@Operation(summary = "사용자 탈퇴", description = "회원을 탈퇴한다.")
	@DeleteMapping("/{userId}")
	public Response<?> putUserInfo(
		@Parameter(description = "찾고자 하는 userId", required = true) @PathVariable Long userId,
		@RequestBody UserDeleteRequest userDeleteRequest
	) {
		userService.deleteUser(userId, userDeleteRequest);
		return Response.createSuccessWithNoData();
	}

	@Operation(summary = "FCM 등록 토큰 등록", description = "푸시 알림을 위한 FCM 등록 토큰을 저장한다.")
	@PostMapping("/fcm-token")
	public Response<?> postFcmToken(
		@RequestBody FcmTokenRequest fcmTokenRequest
	) {
		fcmService.registerFcmToken(fcmTokenRequest);
		return Response.createSuccessWithNoData();
	}
}
