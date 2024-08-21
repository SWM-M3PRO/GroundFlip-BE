package com.m3pro.groundflip.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.m3pro.groundflip.domain.dto.version.VersionResponse;
import com.m3pro.groundflip.enums.Version;

@ExtendWith(MockitoExtension.class)
public class VersionServiceTest {

	@InjectMocks
	private VersionService versionService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(versionService, "lastestVersion", "2.0.3");
		ReflectionTestUtils.setField(versionService, "recommandUpdate", "1.0.5");
	}

	@Test
	@DisplayName("[getVersion] version이 잘 get 되는지")
	void getVersionTest() {
		//Given
		String currentVersion = "1.0.3";
		String currentVersion2 = "2.0.1";
		String currentVersion3 = "2.0.4";

		//When
		VersionResponse versionResponse = versionService.getVersion(currentVersion);
		VersionResponse versionResponse2 = versionService.getVersion(currentVersion2);
		VersionResponse versionResponse3 = versionService.getVersion(currentVersion3);

		//Then
		assertThat(versionResponse).isNotNull();
		assertThat(versionResponse.getVersion()).isEqualTo("2.0.3");
		assertThat(versionResponse.getNeedUpdate()).isEqualTo(Version.FORCE);

		assertThat(versionResponse2.getVersion()).isEqualTo("2.0.3");
		assertThat(versionResponse2.getNeedUpdate()).isEqualTo(Version.NEED);

		assertThat(versionResponse3.getVersion()).isEqualTo("2.0.3");
		assertThat(versionResponse3.getNeedUpdate()).isEqualTo(Version.OK);

	}

}
