package com.m3pro.groundflip.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.community.CommunityInfoResponse;
import com.m3pro.groundflip.domain.dto.community.CommunityJoinRequest;
import com.m3pro.groundflip.domain.dto.community.CommunitySearchResponse;
import com.m3pro.groundflip.service.CommunityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/communities")
@Tag(name = "communities", description = "그룹 API")
@SecurityRequirement(name = "Authorization")
public class CommunityController {
	private final CommunityService communityService;

	@Operation(summary = "그룹 검색", description = "그룹 이름 검색 키워드로 그룹들을 검색")
	@Parameter(name = "searchKeyword", description = "그룹 검색 키워드", example = "홍익대")
	@GetMapping("")
	public Response<List<CommunitySearchResponse>> getAllCommunityByName(
		@RequestParam(name = "searchKeyword") String searchKeyword) {
		return Response.createSuccess(
			communityService.findAllCommunityByName(searchKeyword)
		);
	}

	@Operation(summary = "그룹 정보 조회", description = "특정 그룹의 정보를 반환한다.")
	@GetMapping("/{communityId}")
	public Response<CommunityInfoResponse> getCommunityInfo(
		@Parameter(description = "찾고자 하는 userId", required = true)
		@PathVariable Long communityId
	) {
		return Response.createSuccess(communityService.findCommunityById(communityId));
	}

	@Operation(summary = "그룹 가입 api", description = "유저를 특정 그룹에 가입시킨다")
	@PostMapping("/{communityId}")
	public Response<?> joinCommunity(
		@Parameter(description = "가입하고자 하는 groupId", required = true)
		@PathVariable("communityId") Long communityId,
		@RequestBody CommunityJoinRequest communityJoinRequest
	) {
		communityService.joinCommunity(communityId, communityJoinRequest);
		return Response.createSuccessWithNoData();
	}
}
