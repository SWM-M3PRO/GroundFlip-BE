package com.m3pro.groundflip.domain.dto.pixel.NaverAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(title="네이버 api json 결과")
public class NaverAPIResult {


    @JsonProperty("results")
    private List<Result> results;


    public List<String> getAreaNames() {
        List<String> areaNames= new ArrayList<>();
        areaNames.add(this.results.get(0).getRegion().getArea1().getName());
        areaNames.add(this.results.get(0).getRegion().getArea2().getName());
        areaNames.add(this.results.get(0).getRegion().getArea3().getName());
        return areaNames;
    }
}


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Result {

    @JsonProperty("region")
    private Region region;

}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Region {

    private Area area1;

    private Area area2;

    private Area area3;

}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Area {

    private String name;

}


