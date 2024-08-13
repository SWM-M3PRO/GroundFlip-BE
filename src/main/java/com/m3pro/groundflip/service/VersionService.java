package com.m3pro.groundflip.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.version.VersionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VersionService {

	@Value("${version.update}")
	private String value;

	public VersionResponse getVersion() {
		return VersionResponse.builder()
			.version(value)
			.build();
	}
}
