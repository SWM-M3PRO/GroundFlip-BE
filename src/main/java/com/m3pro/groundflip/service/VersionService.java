package com.m3pro.groundflip.service;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.version.VersionRequest;
import com.m3pro.groundflip.domain.dto.version.VersionResponse;
import com.m3pro.groundflip.domain.entity.AppVersion;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.AppVersionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VersionService {
	private final AppVersionRepository appVersionRepository;

	@Transactional
	public void postVersion(VersionRequest versionRequest) {
		appVersionRepository.save(
			AppVersion.builder()
				.version(versionRequest.getVersion())
				.build()
		);
	}

	public VersionResponse getVersion() {
		AppVersion appVersion = appVersionRepository.findLaestetVersion()
			.orElseThrow(() -> new AppException(ErrorCode.VERSION_NOT_FOUND));

		return VersionResponse.builder()
			.version(appVersion.getVersion())
			.createdDate(appVersion.getCreatedDate())
			.build();
	}
}
