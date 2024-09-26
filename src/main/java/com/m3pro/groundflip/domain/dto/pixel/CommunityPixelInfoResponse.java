package com.m3pro.groundflip.domain.dto.pixel;

import java.util.List;

import com.m3pro.groundflip.domain.entity.Pixel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(title = "그룹전 픽셀 정보")
public class CommunityPixelInfoResponse {
	private String address;
	private Integer addressNumber;
	private Integer visitCount;
	private PixelOwnerCommunityResponse pixelOwnerCommunity;
	private List<VisitedCommunityInfo> visitList;

	public static CommunityPixelInfoResponse from(Pixel pixel, PixelOwnerCommunityResponse pixelOwnerCommunityResponse,
		List<VisitedCommunityInfo> visitedCommunityList) {
		String realAddress;

		if (pixel.getAddress() != null) {
			String[] addressArr = pixel.getAddress().split(" ");
			if (addressArr[0].equals("대한민국")) {
				realAddress = addressArr[0];
			} else {
				realAddress = addressArr[1] + ' ' + addressArr[2];
			}
		} else {
			realAddress = null;
		}

		return CommunityPixelInfoResponse.builder()
			.address(realAddress)
			.addressNumber(pixel.getAddressNumber())
			.visitCount(visitedCommunityList.size())
			.pixelOwnerCommunity(pixelOwnerCommunityResponse)
			.visitList(visitedCommunityList)
			.build();
	}
}
