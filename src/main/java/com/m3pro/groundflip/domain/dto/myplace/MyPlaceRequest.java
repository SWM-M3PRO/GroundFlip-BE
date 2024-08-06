package com.m3pro.groundflip.domain.dto.myplace;

import com.m3pro.groundflip.enums.Place;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "사용자 즐겨찾기 정보 등록")
public class MyPlaceRequest {

	@Schema(description = "유저 id", example = "1")
	private Long userId;

	@Schema(description = "즐겨찾기 장소 이름", example = "학교")
	private Place placeName;

	@Schema(description = "위도", example = "37.321147")
	private double latitude;

	@Schema(description = "경도", example = "127.093171")
	private double longitude;

}
