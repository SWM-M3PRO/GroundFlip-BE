package com.m3pro.groundflip.domain.dto.pixel.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PixelUserInsertEvent {
	private Long pixelId;
	private Long userId;
	private Long communityId;
}
