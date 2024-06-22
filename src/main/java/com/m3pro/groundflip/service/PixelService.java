package com.m3pro.groundflip.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.pixel.IndividualPixelResponse;
import com.m3pro.groundflip.repository.PixelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PixelService {
	private final PixelRepository pixelRepository;

	public List<IndividualPixelResponse> getNearIndividualPixels(int currentX, int currentY, int xRange, int yRange) {
		return pixelRepository.findAllNearPixels(currentX, currentY, xRange, yRange)
			.stream()
			.map(IndividualPixelResponse::from)
			.toList();
	}
}
