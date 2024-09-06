package com.m3pro.groundflip.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.version.VersionResponse;
import com.m3pro.groundflip.service.VersionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "version", description = "앱 버전 API")
public class VersionController {
	private final VersionService versionService;

	@Operation(summary = "버전 업데이트 필요 여부 확인", description = "현재 앱 버전 업데이트가 필요한지 판별")
	@GetMapping("/version")
	public Response<VersionResponse> getVersion(
		@Parameter(description = "현재 버전", required = true)
		@RequestParam(name = "currentVersion") String currentVersion
	) {
		return Response.createSuccess(versionService.getVersion(currentVersion));
	}
}
