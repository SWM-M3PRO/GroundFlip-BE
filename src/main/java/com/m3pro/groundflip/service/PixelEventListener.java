package com.m3pro.groundflip.service;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.m3pro.groundflip.domain.dto.pixel.event.PixelAddressUpdateEvent;
import com.m3pro.groundflip.domain.dto.pixel.event.PixelUserInsertEvent;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.repository.PixelRepository;
import com.m3pro.groundflip.repository.PixelUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PixelEventListener {
	private final PixelUserRepository pixelUserRepository;
	private final ReverseGeoCodingService reverseGeoCodingService;
	private final PixelRepository pixelRepository;

	@EventListener
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Async
	public void insertPixelUserHistory(PixelUserInsertEvent pixelUserInsertEvent) {
		pixelUserRepository.save(
			pixelUserInsertEvent.getPixelId(),
			pixelUserInsertEvent.getUserId(),
			pixelUserInsertEvent.getCommunityId()
		);
	}

	@EventListener
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Async
	public void updatePixelAddress(PixelAddressUpdateEvent pixelAddressUpdateEvent) {
		Pixel targetPixel = pixelAddressUpdateEvent.getPixel();
		String address = reverseGeoCodingService.getAddressFromCoordinates(targetPixel.getCoordinate().getX(),
			targetPixel.getCoordinate().getY());

		targetPixel.updateAddress(address);
		pixelRepository.save(targetPixel);
	}
}
