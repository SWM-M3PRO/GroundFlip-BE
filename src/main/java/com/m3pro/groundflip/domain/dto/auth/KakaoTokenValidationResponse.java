package com.m3pro.groundflip.domain.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoTokenValidationResponse {
    @JsonProperty("app_id")
    private Integer appId;
}
