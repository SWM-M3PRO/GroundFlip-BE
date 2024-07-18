package com.m3pro.groundflip.domain.dto.pixel.naverApi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Result {

	@JsonProperty("region")
	private Region region;

}
