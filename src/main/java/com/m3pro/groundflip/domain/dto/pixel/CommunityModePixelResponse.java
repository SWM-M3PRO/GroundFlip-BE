package com.m3pro.groundflip.domain.dto.pixel;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "개인전 픽셀 정보")
public interface CommunityModePixelResponse {
	@Schema(description = "픽셀 ID", example = "78611")
	Long getPixelId();

	@Schema(description = "픽셀 좌측 상단 위도", example = "37.503717")
	double getLatitude();

	@Schema(description = "픽셀 좌측 상단 경도", example = "127.044317")
	double getLongitude();

	@Schema(description = "소유 그룹의 ID", example = "3")
	Long getCommunityId();

	@Schema(description = "소유 그룹의 ID", example = "3")
	String getCommunityColor();

	@Schema(description = "픽셀 세로 상대 좌표", example = "224")
	Integer getX();

	@Schema(description = "픽셀 가로 상대 좌표", example = "210")
	Integer getY();
}
