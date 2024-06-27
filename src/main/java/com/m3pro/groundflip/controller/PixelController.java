package com.m3pro.groundflip.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.pixel.IndividualHistoryPixelResponse;
import com.m3pro.groundflip.domain.dto.pixel.IndividualPixelResponse;
import com.m3pro.groundflip.domain.dto.pixel.PixelOccupyRequest;
import com.m3pro.groundflip.service.PixelService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pixels")
public class PixelController {
	private final PixelService pixelService;

	@Operation(summary = "개인전 픽셀 조회", description = "특정 좌표를 중심으로 반경 내 개인전 픽셀 정보를 조회 API")
	@Parameters({
		@Parameter(name = "current-latitude", description = "원의 중심 좌표의 위도", example = "37.503717"),
		@Parameter(name = "current-longitude", description = "원의 중심 좌표의 경도", example = "127.044317"),
		@Parameter(name = "radius", description = "미터 단위의 반경", example = "1000"),
	})
	@GetMapping("/individual-mode")
	public Response<List<IndividualPixelResponse>> getNearIndividualPixels(
		@RequestParam(name = "current-latitude") double currentLatitude,
		@RequestParam(name = "current-longitude") double currentLongitude,
		@RequestParam(name = "radius") int radius) {
		return Response.createSuccess(
			pixelService.getNearIndividualPixelsByCoordinate(currentLatitude, currentLongitude, radius));
	}

	@Operation(summary = "개인기록 픽셀 조회", description = "특정 좌표를 중심으로 반경 내 개인 기록 픽셀 정보를 조회 API")
	@Parameters({
		@Parameter(name = "current-latitude", description = "원의 중심 좌표의 위도", example = "37.503717"),
		@Parameter(name = "current-longitude", description = "원의 중심 좌표의 경도", example = "127.044317"),
		@Parameter(name = "radius", description = "미터 단위의 반경", example = "1000"),
	})
	@GetMapping("/individual-history")
	public Response<List<IndividualHistoryPixelResponse>> getNearIndividualHistoryPixels(
		@RequestParam(name = "current-latitude") double currentLatitude,
		@RequestParam(name = "current-longitude") double currentLongitude,
		@RequestParam(name = "radius") int radius,
		@RequestParam(name = "user-id") Long userId) {
		return Response.createSuccess(
			pixelService.getNearIndividualHistoryPixelsByCoordinate(currentLatitude, currentLongitude, radius, userId)
		);
	}

	@Operation(summary = "픽셀 차지", description = "특정 픽셀의 id, 사용자 id, 커뮤니티 id를 사용해 소유권을 바꾸는 API ")
	@PostMapping("")
	public Response<?> occupyPixel(@RequestBody PixelOccupyRequest pixelOccupyRequest) {
		pixelService.occupyPixel(pixelOccupyRequest);
		return Response.createSuccessWithNoData();
	}

}
