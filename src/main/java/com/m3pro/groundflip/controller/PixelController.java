package com.m3pro.groundflip.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.pixel.IndividualPixelResponse;
import com.m3pro.groundflip.service.PixelService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pixels")
public class PixelController {
	private final PixelService pixelService;

	@GetMapping("/individual")
	public Response<List<IndividualPixelResponse>> getNearIndividualPixels(
		@RequestParam(name = "current-x") int currentX,
		@RequestParam(name = "current-y") int currentY,
		@RequestParam(name = "x-range", required = false, defaultValue = "20") int xRange,
		@RequestParam(name = "y-range", required = false, defaultValue = "10") int yRange) {
		return Response.createSuccess(pixelService.getNearIndividualPixels(currentX, currentY, xRange, yRange));
	}

}
