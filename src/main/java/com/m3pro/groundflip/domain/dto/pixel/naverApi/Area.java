package com.m3pro.groundflip.domain.dto.pixel.naverApi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Area {

	private String name;

}
