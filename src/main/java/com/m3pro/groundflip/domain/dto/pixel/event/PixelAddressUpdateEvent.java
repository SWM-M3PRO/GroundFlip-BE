package com.m3pro.groundflip.domain.dto.pixel.event;

import com.m3pro.groundflip.domain.entity.Pixel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PixelAddressUpdateEvent {
	private Pixel pixel;
}
