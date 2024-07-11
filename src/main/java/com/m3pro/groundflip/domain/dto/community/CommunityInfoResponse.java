package com.m3pro.groundflip.domain.dto.community;

import com.m3pro.groundflip.domain.entity.Community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CommunityInfoResponse {
	private String name;
	private String pixelColor;
	private String profileImageUrl;
	private String backgroundImageUrl;
	private int groupRanking;
	private int memberCount;

	public static CommunityInfoResponse from(Community community, int groupRanking, int memberCount) {
		return CommunityInfoResponse.builder()
			.name(community.getName())
			.pixelColor(community.getPixelColor())
			.profileImageUrl(community.getProfileImageUrl())
			.backgroundImageUrl(community.getBackgroundImageUrl())
			.groupRanking(groupRanking)
			.memberCount(memberCount)
			.build();
	}
}
