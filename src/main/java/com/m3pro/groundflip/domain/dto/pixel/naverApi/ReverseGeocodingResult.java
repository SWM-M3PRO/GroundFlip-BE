package com.m3pro.groundflip.domain.dto.pixel.naverApi;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(title = "네이버 api json 결과")
public class ReverseGeocodingResult {
	@JsonProperty("region")
	private String regionName;

	@JsonProperty("region_id")
	private Long regionId;
}
