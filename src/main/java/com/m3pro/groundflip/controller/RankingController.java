package com.m3pro.groundflip.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.ranking.UserRankingResponse;
import com.m3pro.groundflip.service.RankingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/ranking")
@Tag(name = "ranking", description = "랭킹 API")
@SecurityRequirement(name = "Authorization")
public class RankingController {
	private final RankingService rankingService;

	@Operation(summary = "개인전 전체 랭킹 조회", description = "현재 개인전 유저들의 차지 중인 픽셀 기준으로 상위 30명의 랭킹을 반환한다.")
	@GetMapping("/user")
	public Response<List<UserRankingResponse>> getAllUserRanking() {
		return Response.createSuccess(
			rankingService.getAllUserRanking());
	}

	@Operation(summary = "개인전 개인 랭킹 조회", description = "특정 유저의 현재 순위를 반환한다.")
	@GetMapping("/user/{userId}")
	public Response<UserRankingResponse> getUserRank(
		@Parameter(description = "찾고자 하는 userID", required = true)
		@PathVariable Long userId
	) {
		return Response.createSuccess(
			rankingService.getUserRankInfo(userId));
	}
}
