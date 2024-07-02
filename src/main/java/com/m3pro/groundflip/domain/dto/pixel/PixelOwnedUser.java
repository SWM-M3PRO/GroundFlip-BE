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
public class PixelOwnedUser {
	private String nickname;
	private String profileImageUrl;
	private Integer currentPixelCount;
	private Integer accumulatePixelCount;

	public static PixelOwnedUser from(PixelOwnerUser pixelOwnerUser, PixelCount currentPixelCount,
		PixelCount accumulatePixelCount) {
		return PixelOwnedUser.builder()
			.nickname(pixelOwnerUser.getNickname())
			.profileImageUrl(pixelOwnerUser.getProfileImage())
			.accumulatePixelCount(accumulatePixelCount.getCount())
			.currentPixelCount(currentPixelCount.getCount())
			.build();
	}
}
