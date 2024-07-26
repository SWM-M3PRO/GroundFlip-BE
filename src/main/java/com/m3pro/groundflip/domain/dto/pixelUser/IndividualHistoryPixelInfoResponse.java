package com.m3pro.groundflip.domain.dto.pixelUser;

import java.time.LocalDateTime;
import java.util.List;

import com.m3pro.groundflip.domain.entity.Pixel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class IndividualHistoryPixelInfoResponse {
	private String address;
	private Integer addressNumber;
	private Integer visitCount;
	private List<LocalDateTime> visitList;

	public static IndividualHistoryPixelInfoResponse from(Pixel pixel, List<LocalDateTime> visitList) {
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

		return IndividualHistoryPixelInfoResponse
			.builder()
			.address(realAddress)
			.addressNumber(pixel.getAddressNumber())
			.visitCount(visitList.size())
			.visitList(visitList)
			.build();
	}
}
