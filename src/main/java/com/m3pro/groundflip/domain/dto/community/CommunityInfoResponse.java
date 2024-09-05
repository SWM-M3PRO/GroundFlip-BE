package com.m3pro.groundflip.domain.dto.community;

import com.m3pro.groundflip.domain.entity.Community;

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
public class CommunityInfoResponse {

	@Schema(description = "그룹 이름", example = "Pixel Group")
	private String name;

	@Schema(description = "그룹 색상", example = "#FF5733")
	private String communityColor;

	@Schema(description = "그룹 배경 이미지 URL", example = "https://example.com/background.jpg")
	private String backgroundImageUrl;

	@Schema(description = "그룹 랭킹", example = "1")
	private int communityRanking;

	@Schema(description = "그룹 멤버 수", example = "500")
	private int memberCount;

	@Schema(description = "현재 차지하고 있는 픽셀 개수", example = "5")
	private int currentPixelCount;

	@Schema(description = "누적 픽셀 개수", example = "1000")
	private int accumulatePixelCount;

	@Schema(description = "최대 픽셀 개수", example = "5000")
	private int maxPixelCount;

	@Schema(description = "최고 랭킹", example = "1")
	private int maxRanking;

	public static CommunityInfoResponse from(Community community, int communityRanking, int memberCount,
		int currentPixelCount, int accumulatePixelCount) {
		return CommunityInfoResponse.builder()
			.name(community.getName())
			.communityColor(community.getPixelColor())
			.backgroundImageUrl(community.getBackgroundImageUrl())
			.maxPixelCount(community.getMaxPixelCount())
			.maxRanking(community.getMaxRanking())
			.currentPixelCount(currentPixelCount)
			.accumulatePixelCount(accumulatePixelCount)
			.communityRanking(communityRanking)
			.memberCount(memberCount)
			.build();
	}
}
