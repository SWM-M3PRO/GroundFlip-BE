package com.m3pro.groundflip.domain.dto.ranking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(title = "사용자 랭킹")
public class UserRankingResponse {
	@Schema(description = "사용자 ID", example = "1234")
	private Long userId;

	@Schema(description = "사용자 닉네임", example = "홍길동")
	private String nickname;

	@Schema(description = "사용자 프로필 사진 주소", example = "http://www.test.com")
	private Long profileImageUrl;

	@Schema(description = "현재 차지하고 있는 픽셀 개수", example = "5")
	private Long currentPixelCount;

	@Schema(description = "순위", example = "4")
	private Long rank;
}
