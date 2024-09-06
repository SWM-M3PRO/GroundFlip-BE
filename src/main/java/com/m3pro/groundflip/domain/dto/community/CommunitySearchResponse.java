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
public class CommunitySearchResponse {
	private String name;

	private String backgroundImageUrl;

	private String communityColor;

	public static CommunitySearchResponse from(Community community) {
		return CommunitySearchResponse.builder()
			.name(community.getName())
			.backgroundImageUrl(community.getBackgroundImageUrl())
			.communityColor(community.getCommunityColor())
			.build();
	}

}
