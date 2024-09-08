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
	private Long communityId = -1L;

	@Schema(description = "픽셀의 세로 상대좌표", example = "222")
	private Long x;

	@Schema(description = "픽셀의 가로 상대좌표", example = "233")
	private Long y;
}
