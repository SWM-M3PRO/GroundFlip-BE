package com.m3pro.groundflip.domain.dto.pixel;

import org.locationtech.jts.geom.Point;

import com.m3pro.groundflip.config.GeometryConverter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(title = "개인전 픽셀 정보")
public class IndividualPixelResponse {
	@Schema(description = "픽셀 ID", example = "78611")
	private long pixelId;

	@Schema(description = "픽셀 좌측 상단 위도", example = "37.503717")
	private double latitude;

	@Schema(description = "픽셀 좌측 상단 경도", example = "127.044317")
	private double longitude;

	@Schema(description = "소유주의 ID", example = "3")
	private long userId;

	@Schema(description = "픽셀 세로 상대 좌표", example = "224")
	private long x;

	@Schema(description = "픽셀 가로 상대 좌표", example = "210")
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
