package com.m3pro.groundflip.domain.dto.pixel;

import com.m3pro.groundflip.domain.dto.pixelUser.PixelCount;
import com.m3pro.groundflip.domain.dto.pixelUser.PixelOwnerUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PixelOwnerUserResponse {
	private Long userId;
	private String nickname;
	private String profileImageUrl;
	private Integer currentPixelCount;
	private Integer accumulatePixelCount;

	public static PixelOwnerUserResponse from(PixelOwnerUser pixelOwnerUser, PixelCount currentPixelCount,
		PixelCount accumulatePixelCount) {
		if (pixelOwnerUser == null) {
			return null;
		} else {
			return PixelOwnerUserResponse.builder()
				.userId(pixelOwnerUser.getUserId())
				.nickname(pixelOwnerUser.getNickname())
				.profileImageUrl(pixelOwnerUser.getProfileImage())
				.accumulatePixelCount(accumulatePixelCount.getCount())
				.currentPixelCount(currentPixelCount.getCount())
				.build();
		}
	}
}