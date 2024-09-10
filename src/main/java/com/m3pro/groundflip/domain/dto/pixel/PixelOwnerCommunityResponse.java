package com.m3pro.groundflip.domain.dto.pixel;

import com.m3pro.groundflip.domain.entity.Community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PixelOwnerCommunityResponse {
	private Long communityId;
	private String name;
	private String profileImageUrl;
	private Long currentPixelCount;
	private Long accumulatePixelCount;

	public static PixelOwnerCommunityResponse from(Community pixelOwnerCommunity, Long currentPixelCount,
		Long accumulatePixelCount) {
		if (pixelOwnerCommunity == null) {
			return null;
		} else {
			return PixelOwnerCommunityResponse.builder()
				.communityId(pixelOwnerCommunity.getId())
				.name(pixelOwnerCommunity.getName())
				.profileImageUrl(pixelOwnerCommunity.getBackgroundImageUrl())
				.accumulatePixelCount(accumulatePixelCount)
				.currentPixelCount(currentPixelCount)
				.build();
		}
	}
}
