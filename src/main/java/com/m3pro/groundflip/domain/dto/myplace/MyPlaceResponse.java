package com.m3pro.groundflip.domain.dto.myplace;

import org.locationtech.jts.geom.Point;

import com.m3pro.groundflip.domain.entity.MyPlace;
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
@Schema(title = "사용자 즐겨찾기 정보 검색")
public class MyPlaceResponse {

	@Schema(description = "즐겨찾기 id", example = "1")
	private Long id;

	@Schema(description = "즐겨찾기 장소 이름", example = "학교")
	private Place placeName;

	@Schema(description = "즐겨찾기 장소 좌표", example = "0xE6100000010100000...")
	private Point placePoint;

	public static MyPlaceResponse from(MyPlace myplace) {
		return MyPlaceResponse.builder()
			.id(myplace.getId())
			.placeName(myplace.getPlaceName())
			.placePoint(myplace.getPlacePoint())
			.build();
	}
}
