package com.m3pro.groundflip.domain.dto.pixel;

import com.m3pro.groundflip.domain.dto.pixelUser.VisitedCommunity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class VisitedCommunityInfo {
	private String name;
	private String profileImageUrl;

	public static VisitedCommunityInfo from(VisitedCommunity visitedCommunity) {
		return VisitedCommunityInfo.builder()
			.name(visitedCommunity.getName())
			.profileImageUrl(visitedCommunity.getProfileImage())
			.build();
	}
}
