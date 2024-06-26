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
public class CommunitySearchListResponse {
	private String name;

	public static CommunitySearchListResponse from(Community community) {
		return CommunitySearchListResponse.builder()
			.name(community.getName())
			.build();
	}

}
