package com.m3pro.groundflip.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.pixel.ClusteredPixelCount;
import com.m3pro.groundflip.domain.dto.pixel.CommunityModePixelResponse;
import com.m3pro.groundflip.domain.dto.pixel.CommunityPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixel.IndividualHistoryPixelResponse;
import com.m3pro.groundflip.domain.dto.pixel.IndividualModePixelResponse;
import com.m3pro.groundflip.domain.dto.pixel.IndividualPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixel.PixelCountResponse;
import com.m3pro.groundflip.domain.dto.pixel.PixelOccupyRequest;
import com.m3pro.groundflip.domain.dto.pixelUser.IndividualHistoryPixelInfoResponse;
import com.m3pro.groundflip.service.PixelManager;
import com.m3pro.groundflip.service.PixelReader;
import com.m3pro.groundflip.service.RegionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/pixels")
@Tag(name = "pixels", description = "픽셀 API")
@SecurityRequirement(name = "Authorization")
public class PixelController {
	private final PixelManager pixelManager;
	private final PixelReader pixelReader;
	private final RegionService regionService;

	@Operation(summary = "개인전 픽셀 조회", description = "특정 좌표를 중심으로 반경 내 개인전 픽셀 정보를 조회 API")
	@Parameters({
		@Parameter(name = "current-latitude", description = "원의 중심 좌표의 위도", example = "37.503717"),
		@Parameter(name = "current-longitude", description = "원의 중심 좌표의 경도", example = "127.044317"),
		@Parameter(name = "radius", description = "미터 단위의 반경", example = "1000"),
	})
	@GetMapping("/individual-mode")
	public Response<List<IndividualModePixelResponse>> getNearIndividualPixels(
		@RequestParam(name = "current-latitude") @Min(-90) @Max(90) double currentLatitude,
		@RequestParam(name = "current-longitude") @Min(-180) @Max(180) double currentLongitude,
		@RequestParam(name = "radius") @Min(0) int radius) {
		return Response.createSuccess(
			pixelReader.getNearIndividualModePixelsByCoordinate(currentLatitude, currentLongitude, radius));
	}

	@GetMapping("/individual-mode/test")
	public Response<List<IndividualModePixelResponse>> getNearIndividualPixelsTest(
		@RequestParam(name = "x") int x,
		@RequestParam(name = "y") int y,
		@RequestParam(name = "width") int width,
		@RequestParam(name = "height") int height
	) {
		return Response.createSuccess(
			pixelReader.getNeaerIndividualModePixelsTest(x, y, width, height));
	}

	@Operation(summary = "그룹전 픽셀 조회", description = "특정 좌표를 중심으로 반경 내 개인전 픽셀 정보를 조회 API")
	@Parameters({
		@Parameter(name = "current-latitude", description = "원의 중심 좌표의 위도", example = "37.503717"),
		@Parameter(name = "current-longitude", description = "원의 중심 좌표의 경도", example = "127.044317"),
		@Parameter(name = "radius", description = "미터 단위의 반경", example = "1000"),
	})
	@GetMapping("/community-mode")
	public Response<List<CommunityModePixelResponse>> getNearCommunityPixels(
		@RequestParam(name = "current-latitude") @Min(-90) @Max(90) double currentLatitude,
		@RequestParam(name = "current-longitude") @Min(-180) @Max(180) double currentLongitude,
		@RequestParam(name = "radius") @Min(0) int radius) {
		return Response.createSuccess(
			pixelReader.getNearCommunityModePixelsByCoordinate(currentLatitude, currentLongitude, radius));
	}

	@Operation(summary = "개인기록 픽셀 조회", description = "특정 좌표를 중심으로 반경 내 개인 기록 픽셀 정보를 조회 API")
	@Parameters({
		@Parameter(name = "current-latitude", description = "원의 중심 좌표의 위도", example = "37.503717"),
		@Parameter(name = "current-longitude", description = "원의 중심 좌표의 경도", example = "127.044317"),
		@Parameter(name = "radius", description = "미터 단위의 반경", example = "1000"),
		@Parameter(name = "user-id", description = "찾고자 하는 사용자의 id", example = "1"),
	})
	@GetMapping("/individual-history")
	public Response<List<IndividualHistoryPixelResponse>> getNearIndividualHistoryPixels(
		@RequestParam(name = "current-latitude") @Min(-90) @Max(90) double currentLatitude,
		@RequestParam(name = "current-longitude") @Min(-180) @Max(180) double currentLongitude,
		@RequestParam(name = "radius") @Min(0) int radius,
		@RequestParam(name = "user-id") @NotNull() Long userId,
		@RequestParam(required = false, name = "lookup-date")
		@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate lookUpDate) {
		return Response.createSuccess(
			pixelReader
				.getNearIndividualHistoryPixelsByCoordinate(currentLatitude, currentLongitude, radius, userId,
					lookUpDate)
		);
	}

	@Operation(summary = "개인전 픽셀 정보 조회", description = "특정 개인전 픽셀의 정보를 조회 API")
	@GetMapping("/individual-mode/{pixelId}")
	public Response<IndividualPixelInfoResponse> getIndividualPixelInfo(
		@Parameter(description = "찾고자 하는 pixelId", required = true)
		@PathVariable Long pixelId) {
		return Response.createSuccess(
			pixelReader.getIndividualModePixelInfo(pixelId)
		);
	}

	@Operation(summary = "개인 기록 픽셀 정보 조회", description = "픽셀의 개인 기록 정보를 조회 API")
	@GetMapping("/individual-history/{pixelId}")
	public Response<IndividualHistoryPixelInfoResponse> getIndividualPixelInfo(
		@Parameter(description = "찾고자 하는 pixelId", required = true)
		@PathVariable Long pixelId,
		@Parameter(description = "조회하고자 하는 userId", required = true)
		@RequestParam(name = "user-id") Long userId,
		@RequestParam(required = false, name = "lookup-date")
		@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate lookUpDate
	) {
		return Response.createSuccess(
			pixelReader.getIndividualHistoryPixelInfo(pixelId, userId, lookUpDate)
		);
	}

	@Operation(summary = "그룹전 픽셀 정보 조회", description = "특정 그룹전 픽셀의 정보를 조회 API")
	@GetMapping("/community-mode/{pixelId}")
	public Response<CommunityPixelInfoResponse> getCommunityPixelInfo(
		@Parameter(description = "찾고자 하는 pixelId", required = true)
		@PathVariable Long pixelId) {
		return Response.createSuccess(
			pixelReader.getCommunityModePixelInfo(pixelId)
		);
	}

	@Operation(summary = "픽셀 차지", description = "특정 픽셀의 id, 사용자 id, 커뮤니티 id를 사용해 소유권을 바꾸는 API ")
	@PostMapping("")
	public Response<?> occupyPixel(@RequestBody PixelOccupyRequest pixelOccupyRequest) {
		pixelManager.occupyPixelWithLock(pixelOccupyRequest);
		return Response.createSuccessWithNoData();
	}

	@Operation(summary = "픽셀 개수 조회", description = "특정 유저의 현재 소유중인 픽셀, 누적 픽셀을 조회하는 api")
	@GetMapping("/count")
	public Response<PixelCountResponse> getPixelCount(@RequestParam(name = "user-id") @NotNull() Long userId,
		@RequestParam(required = false, name = "lookup-date")
		@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate lookUpDate) {
		return Response.createSuccess(pixelReader.getPixelCount(userId, lookUpDate));
	}

	@Operation(summary = "픽셀 개수 조회", description = "특정 유저의 현재 소유중인 픽셀, 누적 픽셀을 조회하는 api")
	@GetMapping("/community/count")
	public Response<PixelCountResponse> getCommunityPixelCount(
		@RequestParam(name = "community-id") @NotNull() Long communityId) {
		return Response.createSuccess(pixelReader.getCommunityPixelCount(communityId));
	}

	@Operation(summary = "개인전 클러스터링된 픽셀 조회", description = "특정 좌표를 중심으로 반경 내 개인전 클러스터링된 픽셀 정보 API")
	@Parameters({
		@Parameter(name = "current-latitude", description = "원의 중심 좌표의 위도", example = "37.503717"),
		@Parameter(name = "current-longitude", description = "원의 중심 좌표의 경도", example = "127.044317"),
		@Parameter(name = "radius", description = "미터 단위의 반경", example = "1000"),
	})
	@GetMapping("/individual-mode/clustered")
	public Response<List<ClusteredPixelCount>> getNearIndividualClusteredPixels(
		@RequestParam(name = "current-latitude") @Min(-90) @Max(90) double currentLatitude,
		@RequestParam(name = "current-longitude") @Min(-180) @Max(180) double currentLongitude,
		@RequestParam(name = "radius") @Min(0) int radius) {

		return Response.createSuccess(
			regionService.getIndividualModeClusteredPixelCount(currentLatitude, currentLongitude, radius));
	}

	@Operation(summary = "개인기록 클러스터링된 픽셀 조회", description = "특정 좌표를 중심으로 반경 내 개인기록 클러스터링된 픽셀 정보 API")
	@Parameters({
		@Parameter(name = "current-latitude", description = "원의 중심 좌표의 위도", example = "37.503717"),
		@Parameter(name = "current-longitude", description = "원의 중심 좌표의 경도", example = "127.044317"),
		@Parameter(name = "radius", description = "미터 단위의 반경", example = "1000"),
		@Parameter(name = "user-id", description = "찾고자 하는 user의 id", example = "127"),
	})
	@GetMapping("/individual-history/clustered")
	public Response<List<ClusteredPixelCount>> getNearIndividualHistoryClusteredPixels(
		@RequestParam(name = "current-latitude") @Min(-90) @Max(90) double currentLatitude,
		@RequestParam(name = "current-longitude") @Min(-180) @Max(180) double currentLongitude,
		@RequestParam(name = "radius") @Min(0) int radius,
		@RequestParam(name = "user-id") @NotNull() Long userId) {

		return Response.createSuccess(
			regionService.getIndividualHistoryClusteredPixelCount(currentLatitude, currentLongitude, radius, userId));
	}

	@Operation(summary = "그룹전 클러스터링된 픽셀 조회", description = "특정 좌표를 중심으로 반경 내 그룹전 클러스터링된 픽셀 정보를 조회 API")
	@Parameters({
		@Parameter(name = "current-latitude", description = "원의 중심 좌표의 위도", example = "37.503717"),
		@Parameter(name = "current-longitude", description = "원의 중심 좌표의 경도", example = "127.044317"),
		@Parameter(name = "radius", description = "미터 단위의 반경", example = "1000"),
	})
	@GetMapping("/community-mode/clustered")
	public Response<List<ClusteredPixelCount>> getNearCommunityClusteredPixels(
		@RequestParam(name = "current-latitude") @Min(-90) @Max(90) double currentLatitude,
		@RequestParam(name = "current-longitude") @Min(-180) @Max(180) double currentLongitude,
		@RequestParam(name = "radius") @Min(0) int radius) {

		return Response.createSuccess(
			regionService.getCommunityModeClusteredPixelCount(currentLatitude, currentLongitude, radius));
	}
	
	@Operation(summary = "주간 픽셀 개수 조회", description = "특정 유저의 주간 방문한 픽셀을 조회하는 api")
	@GetMapping("/count/daily/{userId}")
	public Response<List<Integer>> getDailyPixel(
		@PathVariable(name = "userId") @NotNull() Long userId,
		@RequestParam(name = "start-date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
		@RequestParam(name = "end-date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
	) {
		return Response.createSuccess(pixelReader.getDailyPixel(userId, startDate, endDate));
	}
}

