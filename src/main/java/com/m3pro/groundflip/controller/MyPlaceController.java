package com.m3pro.groundflip.controller;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.myplace.MyPlaceRequest;
import com.m3pro.groundflip.service.MyPlaceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/myplace")
@Tag(name = "myplace", description = "즐겨찾기 API")
public class MyPlaceController {
	private final MyPlaceService myPlaceService;

	@Operation(summary = "사용자 즐겨찾기 등록", description = "장소의 좌표, 이름을 저장한다")
	@PutMapping("")
	public Response<?> putMyPlace(
		@Parameter(description = "즐겨찾기 저장 userId", required = true)
		@RequestBody MyPlaceRequest myPlaceRequest
	) {
		myPlaceService.putMyPlace(myPlaceRequest);
		return Response.createSuccessWithNoData();
	}

}
