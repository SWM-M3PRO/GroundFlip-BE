package com.m3pro.groundflip.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.myplace.MyPlaceRequest;
import com.m3pro.groundflip.domain.dto.myplace.MyPlaceResponse;
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

	@Operation(summary = "사용자 즐겨찾기 get", description = "즐겨찾기 장소의 좌표, 이름을 가져온다")
	@GetMapping("/{userId}")
	public Response<List<MyPlaceResponse>> getMyPlace(
		@Parameter(description = "찾고자 하는 userId", required = true)
		@PathVariable Long userId
	) {
		return Response.createSuccess(myPlaceService.getMyPlace(userId));
	}

	@Operation(summary = "사용자 즐겨찾기 delete", description = "즐겨찾기 장소를 삭제한다")
	@DeleteMapping("")
	public Response<?> deleteMyPlace(
		@Parameter(description = "지우고자 하는 userId", required = true)
		@RequestBody MyPlaceRequest myPlaceRequest
	) {
		myPlaceService.deleteMyPlace(myPlaceRequest);
		return Response.createSuccessWithNoData();
	}

}
