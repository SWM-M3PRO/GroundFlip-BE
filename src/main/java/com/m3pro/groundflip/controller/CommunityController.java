package com.m3pro.groundflip.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.community.CommunityInfoResponse;
import com.m3pro.groundflip.domain.dto.community.CommunitySearchResponse;
import com.m3pro.groundflip.service.CommunityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
@SecurityRequirement(name = "Authorization")
public class CommunityController {
	private final CommunityService communityService;

	@Operation(summary = "그룹 검색", description = "그룹 이름 검색 키워드로 그룹들을 검색")
	@Parameter(name = "searchKeyword", description = "그룹 검색 키워드", example = "홍익대")
	@GetMapping("")
	public Response<List<CommunitySearchResponse>> getAllCommunityByName(@RequestParam String searchKeyword) {
		return Response.createSuccess(
			communityService.findAllCommunityByName(searchKeyword)
		);
	}

	@GetMapping("/{groupId}")
	public CommunityInfoResponse findGroupById(@PathVariable Long groupId) {
		return communityService.findCommunityById(groupId);
	}
}
