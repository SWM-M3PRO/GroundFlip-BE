package com.m3pro.groundflip.domain.dto.pixel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(title = "픽셀 개수")
public class PixelCountResponse {
	@Schema(description = "현재 차지하고 있는 픽셀 개수", example = "5")
	private Integer currentPixelCount;

	@Schema(description = "지금까지 차지한 픽셀 개수", example = "5")
	private Integer accumulatePixelCount;
}
