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
	private String latest;

	@Value("${version.recommend}")
	private String recommendUpdate;

	public VersionResponse getVersion(String currentVersion) {
		Version needUpdate = Version.OK;

		if (compareVersions(currentVersion, recommendUpdate) == -1) {
			needUpdate = Version.FORCE;
		}
		if (compareVersions(currentVersion, recommendUpdate) == 1
			&& compareVersions(currentVersion, latest) == -1) {
			needUpdate = Version.NEED;
		}
		if (compareVersions(currentVersion, latest) == 1) {
			needUpdate = Version.OK;
		}

		return VersionResponse.builder()
			.version(latest)
			.needUpdate(needUpdate)
			.build();
	}

	private static int compareVersions(String version1, String version2) {
		String[] v1Parts = version1.split("\\.");
		String[] v2Parts = version2.split("\\.");

		int length = Math.max(v1Parts.length, v2Parts.length);

		for (int i = 0; i < length; i++) {
			int v1 = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
			int v2 = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;

			if (v1 < v2) {
				return -1;
			}
			if (v1 > v2) {
				return 1;
			}
		}
		return 1;
	}
}
