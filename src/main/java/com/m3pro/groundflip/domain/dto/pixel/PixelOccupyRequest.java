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
@Schema(title = "픽셀 차지 요청 Body")
public class PixelOccupyRequest {
	@Schema(description = "사용자 ID", example = "5")
	private Long userId;

	@Schema(description = "커뮤니티 ID", nullable = true, example = "78611")
	private Long communityId;

	@Schema(description = "픽셀 ID", example = "78611")
	private Long pixelId;
}
