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

        return IndividualPixelInfoResponse.builder()
                .address(realAddress)
                .addressNumber(pixel.getAddressNumber())
                .visitCount(visitedUserList.size())
                .pixelOwnerUser(pixelOwnerUserResponse)
                .visitList(visitedUserList)
                .build();
    }
}
