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

	/**
	 * 픽셀 방문 기록을 DB에 삽입한다.
	 * @param pixelUserInsertEvent pixel_user의 필드를 가지는 객체
	 * @return X
	 * @author 김민욱
	 */
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

	/**
	 * 픽셀을 방문 시 픽셀의 주소를 불러온다.
	 * @param pixelAddressUpdateEvent 픽셀 엔티티를 담고있는 객체
	 * @return X
	 * @author 김민욱
	 */
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
