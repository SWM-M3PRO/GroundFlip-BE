package com.m3pro.groundflip.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.version.VersionRequest;
import com.m3pro.groundflip.domain.dto.version.VersionResponse;
import com.m3pro.groundflip.domain.entity.AppVersion;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.AppVersionRepository;

@ExtendWith(MockitoExtension.class)
public class VersionServiceTest {
	@Mock
	private AppVersionRepository appVersionRepository;

	@InjectMocks
	private VersionService versionService;

	@Test
	@DisplayName("[postVersion] version이 잘 post 되는지")
	void postVersionTest() {
		//Given
		VersionRequest versionRequest = VersionRequest.builder().version("1.0.3").build();
		//When
		versionService.postVersion(versionRequest);

		//Then
		verify(appVersionRepository, times(1)).save(any(AppVersion.class));
	}

	@Test
	@DisplayName("[getVersion] version이 잘 get 되는지")
	void getVersionTest() {
		//Given
		LocalDateTime localDate = LocalDateTime.now();
		AppVersion appVersion = AppVersion.builder()
			.version("1.0.3")
			.createdDate(localDate)
			.build();

		when(appVersionRepository.findLaestetVersion()).thenReturn(Optional.of(appVersion));

		//When
		VersionResponse versionResponse = versionService.getVersion();

		//Then
		assertThat(versionResponse).isNotNull();
		assertThat(versionResponse.getVersion()).isEqualTo("1.0.3");
		assertThat(versionResponse.getCreatedDate()).isEqualTo(localDate);

	}

	@Test
	@DisplayName("[getVersion] version이 없을때 예외가 잘 나오는지")
	void getVersionExceptionTest() {
		//When
		when(appVersionRepository.findLaestetVersion()).thenReturn(Optional.empty());

		AppException thrown = assertThrows(AppException.class, () -> {
			versionService.getVersion();
		});

		assertEquals(thrown.getErrorCode(), ErrorCode.VERSION_NOT_FOUND);
	}

}
