package com.m3pro.groundflip.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.ranking.CommunityRankingResponse;
import com.m3pro.groundflip.domain.dto.ranking.UserRankingResponse;
import com.m3pro.groundflip.service.CommunityRankingService;
import com.m3pro.groundflip.service.UserRankingService;

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
	private final UserRankingService userRankingService;
	private final CommunityRankingService communityRankingService;

	@Operation(summary = "개인전 전체 랭킹 조회", description = "현재 개인전 유저들의 차지 중인 픽셀 기준으로 상위 30명의 랭킹을 반환한다.")
	@GetMapping("/user")
	public Response<List<UserRankingResponse>> getAllUserRanking(
		@RequestParam(required = false, name = "lookup-date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate lookUpDate) {
		return Response.createSuccess(userRankingService.getCurrentPixelAllUserRankings(lookUpDate));
	}

	@Operation(summary = "개인전 개인 랭킹 조회", description = "특정 유저의 현재 순위를 반환한다.")
	@GetMapping("/user/{userId}")
	public Response<UserRankingResponse> getUserRank(
		@Parameter(description = "찾고자 하는 userID", required = true) @PathVariable("userId") Long userId,
		@RequestParam(required = false, name = "lookup-date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate lookUpDate) {
		return Response.createSuccess(userRankingService.getUserCurrentPixelRankInfo(userId, lookUpDate));
	}

	@Operation(summary = "그룹 전체 랭킹 조회", description = "현재 그룹들의 차지 중인 픽셀 기준으로 상위 30개의 랭킹을 반환한다.")
	@GetMapping("/community")
	public Response<List<CommunityRankingResponse>> getAllCommunityRanking(
		@RequestParam(required = false, name = "lookup-date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate lookUpDate) {
		return Response.createSuccess(communityRankingService.getCurrentPixelAllUCommunityRankings(lookUpDate));
	}

	@Operation(summary = "그룹 별 랭킹 조회", description = "특정 그룹의 현재 순위를 반환한다.")
	@GetMapping("/community/{communityId}")
	public Response<CommunityRankingResponse> getCommunityRank(
		@Parameter(description = "찾고자 하는 communityId", required = true) @PathVariable("communityId") Long communityId,
		@RequestParam(required = false, name = "lookup-date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate lookUpDate) {
		return Response.createSuccess(
			communityRankingService.getCommunityCurrentPixelRankInfo(communityId, lookUpDate));
	}

	@Operation(summary = "개인전 전체 누적 랭킹 조회", description = "현재 개인전 유저들의 지금 까지 차지한 누적 픽셀 기준으로 상위 100명의 랭킹을 반환한다.")
	@GetMapping("/accumulate/user")
	public Response<List<UserRankingResponse>> getAllUserAccumulateRanking() {
		return Response.createSuccess(userRankingService.getAccumulatePixelAllUserRankings());
	}

	@Operation(summary = "개인전 개인 누적 랭킹 조회", description = "특정 유저의 현재 누적 픽셀 순위를 반환한다.")
	@GetMapping("/accumulate/user/{userId}")
	public Response<UserRankingResponse> getAccumulateRank(
		@Parameter(description = "찾고자 하는 userID", required = true) @PathVariable("userId") Long userId
	) {
		return Response.createSuccess(userRankingService.getUserAccumulatePixelRankInfo(userId));
	}
}
