package com.m3pro.groundflip.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.StepRecord.UserStepInfo;
import com.m3pro.groundflip.domain.dto.user.UserInfoResponse;
import com.m3pro.groundflip.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "users", description = "사용자 API")
public class UserController {
	private final UserService userService;

	@Operation(summary = "사용자 기본 정보 조회", description = "닉네임, id, 출생년도, 성별, 프로필 사진, 그룹이름, 그룹 id 를 조회 한다.")
	@GetMapping("/{userId}")
	public Response<UserInfoResponse> getUserInfo(
		@Parameter(description = "찾고자 하는 userId", required = true)
		@PathVariable Long userId
	) {
		return Response.createSuccess(userService.getUserInfo(userId));
	}

	@Operation(summary = "사용자 걸음수 저장", description = "id, 걸음수, 날짜를 저장한다")
	@PostMapping("/step")
	public Response<?> postUserStep(
		@Parameter(description = "걸음수 저장 userId", required = true)
		@RequestBody UserStepInfo userStepInfo
	) {
		userService.postUserStep(userStepInfo);
		return Response.createSuccessWithNoData();
	}

}
