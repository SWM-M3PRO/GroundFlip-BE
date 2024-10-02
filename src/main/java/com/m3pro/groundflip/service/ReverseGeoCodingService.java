package com.m3pro.groundflip.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.m3pro.groundflip.domain.dto.pixel.naverApi.NaverReverseGeoCodingApiResult;
import com.m3pro.groundflip.domain.dto.pixel.naverApi.ReverseGeocodingResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReverseGeoCodingService {
	private static final String NAVER_API_URL = "https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc";
	private static final String REVERSE_GEOCODING_API_URL = "http://localhost:3030/find_district";

	private final RestClient restClient;
	@Value("${naver.apiKeyId}")
	private String apiKeyId;
	@Value("${naver.apiKey}")
	private String apiKey;

	/**
	 * 특정 좌표의 주소를 얻어온다.
	 * @param longitude 경도
	 * @param latitude 위도
	 * @return 주소 if 대한민국 주소가 없으면 null 반환
	 */
	public ReverseGeocodingResult getRegionFromCoordinates(double longitude, double latitude) {
		return fetchReverseGeoCodingServer(longitude, latitude);
	}

	private ReverseGeocodingResult fetchReverseGeoCodingServer(double longitude, double latitude) {
		URI uri = UriComponentsBuilder.fromHttpUrl(REVERSE_GEOCODING_API_URL)
			.queryParam("lon", longitude)
			.queryParam("lat", latitude)
			.encode(StandardCharsets.UTF_8)
			.build()
			.toUri();

		return restClient.get()
			.uri(uri)
			.retrieve()
			.body(ReverseGeocodingResult.class);
	}

	public String getAddressFromCoordinates(double longitude, double latitude) {
		NaverReverseGeoCodingApiResult naverReverseGeoCodingApiResult =
			fetchNaverReverseGeoCodingApiResult(longitude, latitude);

		if (naverReverseGeoCodingApiResult != null) {
			List<String> areaNames = naverReverseGeoCodingApiResult.getAreaNames();
			return String.join(" ", areaNames);
		} else {
			return null;
		}
	}

	/**
	 * 네이버 리버스 지오 코딩 api 로 부터 주소 정보를 얻어온다.
	 * @param longitude 경도
	 * @param latitude 위도
	 * @return 네이버 리버스 지오 코딩 api 응답 정보
	 */
	private NaverReverseGeoCodingApiResult fetchNaverReverseGeoCodingApiResult(double longitude, double latitude) {
		String coordinate = String.format("%f, %f", longitude, latitude);
		URI uri = UriComponentsBuilder.fromHttpUrl(NAVER_API_URL)
			.queryParam("coords", coordinate)
			.queryParam("orders", "admcode")
			.queryParam("output", "json")
			.encode(StandardCharsets.UTF_8)
			.build()
			.toUri();

		return restClient.get()
			.uri(uri)
			.header("X-NCP-APIGW-API-KEY-ID", apiKeyId)
			.header("X-NCP-APIGW-API-KEY", apiKey)
			.retrieve()
			.body(NaverReverseGeoCodingApiResult.class);
	}
}
