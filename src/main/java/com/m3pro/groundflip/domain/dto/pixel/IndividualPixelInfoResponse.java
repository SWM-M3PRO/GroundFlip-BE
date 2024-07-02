package com.m3pro.groundflip.domain.dto.pixel;

import java.util.List;

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
	private PixelOwnedUser ownUser;
	private List<VisitedUserInfo> visitList;
}
