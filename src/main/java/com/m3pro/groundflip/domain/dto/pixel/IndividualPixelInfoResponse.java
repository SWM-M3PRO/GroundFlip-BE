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
@Schema(title = "개인전 픽셀 정보")
public class IndividualPixelInfoResponse {
	private String address;
	private Integer addressNumber;
	private Integer visitCount;
	private PixelOwnerUserResponse pixelOwnerUser;
	private List<VisitedUserInfo> visitList;

	public static IndividualPixelInfoResponse from(Pixel pixel, PixelOwnerUserResponse pixelOwnerUserResponse,
		List<VisitedUserInfo> visitedUserList) {
		return IndividualPixelInfoResponse.builder()
			.address(pixel.getAddress())
			.addressNumber(pixel.getAddressNumber())
			.visitCount(visitedUserList.size())
			.pixelOwnerUser(pixelOwnerUserResponse)
			.visitList(visitedUserList)
			.build();
	}
}
