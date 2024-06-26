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
		@RequestParam(name = "current-latitude") double currentLatitude,
		@RequestParam(name = "current-longitude") double currentLongitude,
		@RequestParam(name = "radius") int radius) {
		return Response.createSuccess(
			pixelService.getNearIndividualPixelsByCoordinate(currentLatitude, currentLongitude, radius));
	}

}
