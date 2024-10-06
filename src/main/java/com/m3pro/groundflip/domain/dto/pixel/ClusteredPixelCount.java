package com.m3pro.groundflip.domain.dto.pixel;

import com.m3pro.groundflip.enums.RegionLevel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema()
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ClusteredPixelCount {
	@Schema(description = "지역 ID", example = "1234")
	private Long regionId;

	@Schema(description = "지역 이름", example = "홍길동")
	private String regionName;

	@Schema(description = "지역별 픽셀 개수", example = "3")
	private int count;

	@Schema(description = "지역의 중심 위도", example = "37.6464")
	private double latitude;

	@Schema(description = "지역의 중심 경도", example = "127.6897")
	private double longitude;

	@Schema(description = "집계 단위", example = "127.6897")
	private RegionLevel regionLevel;

	public static ClusteredPixelCount from(Long regionId, String regionName, int count, double latitude,
		double longitude, RegionLevel regionLevel) {
		return ClusteredPixelCount.builder()
			.regionId(regionId)
			.regionName(regionName)
			.count(count)
			.latitude(latitude)
			.longitude(longitude)
			.regionLevel(regionLevel)
			.build();
	}
}
