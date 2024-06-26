package com.m3pro.groundflip.domain.dto.pixel;

import org.locationtech.jts.geom.Point;

import com.m3pro.groundflip.config.GeometryConverter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class IndividualPixelResponse {
	private long pixelId;

	private double latitude;

	private double longitude;

	private long userId;

	private long x;

	private long y;

	public static IndividualPixelResponse from(Object[] queryResult) {
		Point coordinate = GeometryConverter.convertGeomToJts(queryResult[1]);

		return IndividualPixelResponse.builder()
			.pixelId((long)queryResult[0])
			.latitude(coordinate.getY())
			.longitude(coordinate.getX())
			.userId((long)queryResult[2])
			.x((long)queryResult[3])
			.y((long)queryResult[4])
			.build();
	}
}
