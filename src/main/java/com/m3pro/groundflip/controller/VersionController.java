package com.m3pro.groundflip.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.version.VersionRequest;
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

	@Operation(summary = "앱 버전 post", description = "현재 앱 버전을 등록한다.")
	@PostMapping("/version")
	public Response<?> postVersion(
		@Parameter(description = "버전 저장", required = true)
		@RequestBody VersionRequest versionRequest
	) {
		versionService.postVersion(versionRequest);
		return Response.createSuccessWithNoData();
	}

	@Operation(summary = "앱 버전 get", description = "현재 앱 버전을 가져온다.")
	@GetMapping("/version")
	public Response<VersionResponse> getVersion(
		@Parameter(description = "버전 get", required = true)
		@RequestParam String version
	) {
		return Response.createSuccess(versionService.getVersion(version));
	}
}
