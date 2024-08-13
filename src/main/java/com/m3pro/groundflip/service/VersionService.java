package com.m3pro.groundflip.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.version.VersionResponse;
import com.m3pro.groundflip.enums.Version;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VersionService {

	@Value("${version.update}")
	private String lastestVersion;

	private Version needUpdate;

	public VersionResponse getVersion(String currentVersion) {
		if (!lastestVersion.equals(currentVersion)) {
			needUpdate = Version.NEED;
		} else {
			needUpdate = Version.OK;
		}

		return VersionResponse.builder()
			.version(lastestVersion)
			.needUpdate(needUpdate)
			.build();
	}
}
