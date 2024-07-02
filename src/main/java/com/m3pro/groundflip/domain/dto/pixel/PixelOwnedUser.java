package com.m3pro.groundflip.domain.dto.pixel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PixelOwnedUser {
	private String name;
	private String profileImageUrl;
	private Integer currentPixelCount;
	private Integer accumulatePixelCount;
}
