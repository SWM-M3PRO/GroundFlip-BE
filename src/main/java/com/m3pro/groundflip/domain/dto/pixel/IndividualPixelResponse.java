package com.m3pro.groundflip.domain.dto.pixel;

import com.m3pro.groundflip.domain.entity.Pixel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IndividualPixelResponse {
	private double latitude;
	private double longitude;
	private double x;
	private double y;

	public static IndividualPixelResponse from(Pixel pixel) {
		return new IndividualPixelResponse(pixel.getLatitude(), pixel.getLongitude(), pixel.getX(), pixel.getY());
	}
}
