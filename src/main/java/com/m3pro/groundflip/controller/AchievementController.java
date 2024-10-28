package com.m3pro.groundflip.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.achievement.AchievementCategoryInfoResponse;
import com.m3pro.groundflip.domain.dto.achievement.AchievementResponse;
import com.m3pro.groundflip.domain.dto.achievement.UserAchievementsResponse;
import com.m3pro.groundflip.service.AchievementService;

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
@RequestMapping("/api/achievements")
@Tag(name = "achievements", description = "업적 API")
@SecurityRequirement(name = "Authorization")
public class AchievementController {
	private final AchievementService achievementService;

	@Operation(summary = "개인 업적 목록 조회", description = "특정 유저가 획득한 업적 목록과 개수를 반환한다.")
	@Parameters({
		@Parameter(name = "user-id", description = "찾고자 하는 user의 id", example = "14"),
		@Parameter(name = "count", description = "반환 받은 업적의 개수", example = "3"),
	})
	@GetMapping("/user")
	public Response<UserAchievementsResponse> getUserAchievements(
		@RequestParam(name = "user-id") Long userId,
		@RequestParam(name = "count", required = false) Integer count
	) {
		return Response.createSuccess(achievementService.getUserAchievements(userId, count));
	}

	@Operation(summary = "업적 정보 조회", description = "업적 정보를 조회하고 사용자의 업적 획득 상태를 반환한다.")
	@Parameters({
		@Parameter(name = "user-id", description = "찾고자 하는 user의 id", example = "14"),
	})
	@GetMapping("/{achievementId}")
	public Response<AchievementResponse> getAchievementInfo(
		@RequestParam(name = "user-id") Long userId,
		@Parameter(description = "찾고자 하는 userId", required = true)
		@PathVariable("achievementId") Long achievementId
	) {
		return Response.createSuccess(achievementService.getAchievement(achievementId, userId));
	}

	@Operation(summary = "카테고리 목록 조회", description = "카테고리 목록을 조회한다.")
	@GetMapping("/category")
	public Response<List<AchievementCategoryInfoResponse>> getAchievementCategories() {
		return Response.createSuccess(achievementService.getAchievementCategories());
	}
}
