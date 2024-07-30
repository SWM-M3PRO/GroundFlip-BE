package com.m3pro.groundflip.domain.dto.pixel;

import com.m3pro.groundflip.domain.entity.User;

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
	private Long currentPixelCount;
	private Long accumulatePixelCount;

	public static PixelOwnerUserResponse from(User pixelOwnerUser, Long currentPixelCount,
		Long accumulatePixelCount) {
		if (pixelOwnerUser == null) {
			return null;
		} else {
			return PixelOwnerUserResponse.builder()
				.userId(pixelOwnerUser.getId())
				.nickname(pixelOwnerUser.getNickname())
				.profileImageUrl(pixelOwnerUser.getProfileImage())
				.accumulatePixelCount(accumulatePixelCount)
				.currentPixelCount(currentPixelCount)
				.build();
		}
	}
}
