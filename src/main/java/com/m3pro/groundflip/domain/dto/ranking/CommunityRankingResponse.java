package com.m3pro.groundflip.domain.dto.ranking;

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
@Schema(title = "그룹 랭킹")
public class CommunityRankingResponse {
	@Schema(description = "그룹 ID", example = "1234")
	private Long communityId;

	@Schema(description = "그룹 닉네임", example = "홍익대학교")
	private String name;

	@Schema(description = "그룹 프로필 사진 주소", example = "http://www.test.com")
	private String profileImageUrl;

	@Schema(description = "현재 차지하고 있는 픽셀 개수", example = "5")
	private Long currentPixelCount;

	@Schema(description = "순위", example = "4")
	private Long rank;

	public static CommunityRankingResponse from(Community community, Long rank, Long currentPixelCount) {
		return CommunityRankingResponse.builder()
			.communityId(community.getId())
			.name(community.getName())
			.profileImageUrl(community.getBackgroundImageUrl())
			.currentPixelCount(currentPixelCount)
			.rank(rank)
			.build();
	}

	public static CommunityRankingResponse from(Community community) {
		return CommunityRankingResponse.builder()
			.communityId(community.getId())
			.name(community.getName())
			.profileImageUrl(community.getBackgroundImageUrl())
			.build();
	}
}
