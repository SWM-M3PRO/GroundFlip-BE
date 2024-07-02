package com.m3pro.groundflip.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.pixel.IndividualPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixelUser.PixelCount;
import com.m3pro.groundflip.domain.dto.pixelUser.PixelOwnerUser;
import com.m3pro.groundflip.domain.dto.pixelUser.VisitedUser;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.PixelRepository;
import com.m3pro.groundflip.repository.PixelUserRepository;

@ExtendWith(MockitoExtension.class)
class PixelServiceTest {
	@Mock
	PixelRepository pixelRepository;
	@Mock
	private PixelUserRepository pixelUserRepository;
	@InjectMocks
	private PixelService pixelService;

	@Test
	@DisplayName("[getIndividualPixelInfo] 없는 pixelId 를 넣을 경우 PIXEL_NOT_FOUND 에러")
	void getIndividualPixelInfoPixelNotFound() {
		// Given
		Long pixelId = 1L;
		when(pixelRepository.findById(pixelId)).thenReturn(Optional.empty());

		// When
		AppException exception = assertThrows(AppException.class, () -> pixelService.getIndividualPixelInfo(pixelId));

		// Then
		assertEquals(ErrorCode.PIXEL_NOT_FOUND, exception.getErrorCode());

	}

	@Test
	@DisplayName("[getIndividualPixelInfo] 없는 pixelId 를 넣을 경우 PIXEL_NOT_FOUND 에러")
	void getIndividualPixelInfoSuccess() {
		// Given
		Long pixelId = 1L;
		String address = "서울";
		int addressNumber = 1;

		Pixel pixel = Pixel.builder()
			.id(pixelId)
			.address(address)
			.addressNumber(addressNumber)
			.build();

		List<VisitedUser> visitedUsers = List.of(
			new VisitedUser() {
				@Override
				public Long getPixelId() {
					return pixelId;
				}

				@Override
				public Long getUserId() {
					return 100L;
				}

				@Override
				public String getNickname() {
					return "JohnDoe";
				}

				@Override
				public String getProfileImage() {
					return "http://profileImage.png";
				}
			}
		);
		PixelOwnerUser pixelOwnerUser = new PixelOwnerUser() {
			@Override
			public Long getUserId() {
				return 100L;
			}

			@Override
			public String getNickname() {
				return "JohnDoe";
			}

			@Override
			public String getProfileImage() {
				return "profileImage.png";
			}
		};
		PixelCount accumulatePixelCount = new PixelCount() {
			@Override
			public int getCount() {
				return 10;
			}
		};
		PixelCount currentPixelCount = new PixelCount() {
			@Override
			public int getCount() {
				return 5;
			}
		};

		when(pixelRepository.findById(pixelId)).thenReturn(Optional.of(pixel));
		when(pixelUserRepository.findAllVisitedUserByPixelId(pixelId)).thenReturn(visitedUsers);
		when(pixelUserRepository.findCurrentOwnerByPixelId(pixelId)).thenReturn(pixelOwnerUser);
		when(pixelUserRepository.findAccumulatePixelCountByUserId(pixelOwnerUser.getUserId())).thenReturn(
			accumulatePixelCount);
		when(pixelUserRepository.findCurrentPixelCountByUserId(pixelOwnerUser.getUserId())).thenReturn(
			currentPixelCount);

		// When
		IndividualPixelInfoResponse response = pixelService.getIndividualPixelInfo(pixelId);

		// Then
		assertThat(response.getAddress()).isEqualTo(address);
		assertThat(response.getAddressNumber()).isEqualTo(addressNumber);
		assertThat(response.getVisitCount()).isEqualTo(visitedUsers.size());
		assertThat(response.getVisitList().get(0).getNickname()).isEqualTo("JohnDoe");
		assertThat(response.getPixelOwnerUser().getCurrentPixelCount()).isEqualTo(currentPixelCount.getCount());
		assertThat(response.getPixelOwnerUser().getNickname()).isEqualTo(pixelOwnerUser.getNickname());
	}

	@Test
	@DisplayName("[getIndividualPixelInfo] pixelId에 해당하는 픽셀에 방문한 사람이 없는 경우")
	void getIndividualPixelInfoNoVisitedUser() {
		// Given
		Long pixelId = 1L;
		String address = "서울";
		int addressNumber = 1;

		Pixel pixel = Pixel.builder()
			.id(pixelId)
			.address(address)
			.addressNumber(addressNumber)
			.build();

		List<VisitedUser> visitedUsers = List.of();
		PixelOwnerUser pixelOwnerUser = null;

		when(pixelRepository.findById(pixelId)).thenReturn(Optional.of(pixel));
		when(pixelUserRepository.findAllVisitedUserByPixelId(pixelId)).thenReturn(visitedUsers);
		when(pixelUserRepository.findCurrentOwnerByPixelId(pixelId)).thenReturn(pixelOwnerUser);

		// When
		IndividualPixelInfoResponse response = pixelService.getIndividualPixelInfo(pixelId);

		// Then
		assertThat(response.getAddress()).isEqualTo(address);
		assertThat(response.getAddressNumber()).isEqualTo(addressNumber);
		assertThat(response.getVisitCount()).isEqualTo(0);
		assertThat(response.getPixelOwnerUser()).isNull();
	}
}
