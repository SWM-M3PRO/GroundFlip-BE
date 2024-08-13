package com.m3pro.groundflip.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.version.VersionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VersionService {

	@Value("${version.update}")
	private String lastestVersion;

	private int needUpdate;

	public VersionResponse getVersion(String currentVersion) {
		if (!lastestVersion.equals(currentVersion)) {
			needUpdate = 1;
		} else {
			needUpdate = 0;
		}

		return VersionResponse.builder()
			.version(lastestVersion)
			.needUpdate(needUpdate)
			.build();
	}
}
