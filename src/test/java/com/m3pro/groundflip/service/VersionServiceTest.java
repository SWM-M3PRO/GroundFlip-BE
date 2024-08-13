package com.m3pro.groundflip.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.version.VersionResponse;

@ExtendWith(MockitoExtension.class)
public class VersionServiceTest {

	@InjectMocks
	private VersionService versionService;

	@Test
	@DisplayName("[getVersion] version이 잘 get 되는지")
	void getVersionTest() {
		//Given
		String currentVersion = "1.0.3";

		//When
		VersionResponse versionResponse = versionService.getVersion(currentVersion);

		//Then
		assertThat(versionResponse).isNotNull();
		assertThat(versionResponse.getVersion()).isEqualTo("1.0.3");

	}

}
