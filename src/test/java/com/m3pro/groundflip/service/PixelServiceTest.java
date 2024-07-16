package com.m3pro.groundflip.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.pixel.IndividualPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixel.PixelCountResponse;
import com.m3pro.groundflip.domain.dto.pixel.PixelOccupyRequest;
import com.m3pro.groundflip.domain.dto.pixelUser.IndividualHistoryPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixelUser.PixelOwnerUser;
import com.m3pro.groundflip.domain.dto.pixelUser.VisitedUser;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.domain.entity.PixelUser;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.CommunityRepository;
import com.m3pro.groundflip.repository.PixelRepository;
import com.m3pro.groundflip.repository.PixelUserRepository;
import com.m3pro.groundflip.repository.UserRepository;
import com.m3pro.groundflip.util.TestUtils;

@ExtendWith(MockitoExtension.class)
class PixelServiceTest {
	@Mock
	private PixelRepository pixelRepository;
	@Mock
	private PixelUserRepository pixelUserRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private CommunityRepository communityRepository;
	@Mock
	private RankingService rankingService;
	@InjectMocks
	private PixelService pixelService;

	@BeforeEach
	void init() {
		reset(pixelRepository);
		reset(pixelUserRepository);
		reset(userRepository);
	}

	@Test
	@DisplayName("[getIndividualModePixelInfo] 없는 pixelId 를 넣을 경우 PIXEL_NOT_FOUND 에러")
	void getIndividualPixelInfoModePixelNotFound() {
		// Given
		Long pixelId = 1L;
		when(pixelRepository.findById(pixelId)).thenReturn(Optional.empty());

		// When
		AppException exception = assertThrows(AppException.class,
			() -> pixelService.getIndividualModePixelInfo(pixelId));

		// Then
		assertEquals(ErrorCode.PIXEL_NOT_FOUND, exception.getErrorCode());

	}

	@Test
	@DisplayName("[getIndividualModePixelInfo] 정상적으로 픽셀에 대한 정보가 있는 경우")
	void getIndividualModePixelInfoSuccess() {
		// Given
		Long pixelId = 1L;
		Long ownerId = 1L;
		String address = "서울특별시 은평구 녹번동";
		int addressNumber = 1;

		Pixel pixel = Pixel.builder()
			.id(pixelId)
			.address(address)
			.addressNumber(addressNumber)
			.userId(ownerId)
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

		User ownerUser = User.builder()
			.id(ownerId)
			.profileImage("www.test.com")
			.nickname("test")
			.build();

		when(pixelRepository.findById(pixelId)).thenReturn(Optional.of(pixel));
		when(pixelUserRepository.findAllVisitedUserByPixelId(pixelId)).thenReturn(visitedUsers);
		when(userRepository.findById(ownerId)).thenReturn(Optional.of(ownerUser));
		when(pixelUserRepository.countAccumulatePixelByUserId(ownerId)).thenReturn(10L);
		when(pixelRepository.countCurrentPixelByUserId(ownerId)).thenReturn(5L);

		// When
		IndividualPixelInfoResponse response = pixelService.getIndividualModePixelInfo(pixelId);

		// Then
		assertThat(response.getAddress()).isEqualTo("은평구 녹번동");
		assertThat(response.getAddressNumber()).isEqualTo(addressNumber);
		assertThat(response.getVisitCount()).isEqualTo(visitedUsers.size());
		assertThat(response.getVisitList().get(0).getNickname()).isEqualTo("JohnDoe");
		assertThat(response.getPixelOwnerUser().getCurrentPixelCount()).isEqualTo(5L);
		assertThat(response.getPixelOwnerUser().getNickname()).isEqualTo("test");
	}

	@Test
	@DisplayName("[getIndividualModePixelInfo] pixelId에 해당하는 픽셀에 방문한 사람이 없는 경우")
	void getIndividualModePixelInfoNoVisitedUser() {
		// Given
		Long pixelId = 1L;
		String address = "서울특별시 은평구 녹번동";
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

		// When
		IndividualPixelInfoResponse response = pixelService.getIndividualModePixelInfo(pixelId);

		// Then
		assertThat(response.getAddress()).isEqualTo("은평구 녹번동");
		assertThat(response.getAddressNumber()).isEqualTo(addressNumber);
		assertThat(response.getVisitCount()).isEqualTo(0);
		assertThat(response.getPixelOwnerUser()).isNull();
	}

	@Test
	@DisplayName("[getIndividualHistoryPixelInfo] pixel history들이 정렬되어 오는지 확인")
	void getIndividualHistoryPixelInfoOrderBy() {
		final int NUMBER_OF_HISTORY = 3;
		// Given
		Long pixelId = 10000L;
		String address = "서울특별시 은평구 녹번동";
		int addressNumber = 1;

		Pixel pixel = Pixel.builder()
			.id(pixelId)
			.address(address)
			.addressNumber(addressNumber)
			.build();

		Long userId = 1L;

		User user = User.builder()
			.id(userId)
			.build();

		List<PixelUser> visitHistory = new ArrayList<>();

		for (int i = 0; i < NUMBER_OF_HISTORY; i++) {
			PixelUser pixelUser = PixelUser.builder()
				.user(user)
				.pixel(pixel)
				.build();
			TestUtils.setCreatedAtOfPixelUser(pixelUser, LocalDateTime.now().minusSeconds(i));
			visitHistory.add(pixelUser);
		}

		// When
		when(pixelRepository.findById(pixelId)).thenReturn(Optional.of(pixel));
		when(pixelUserRepository.findAllVisitHistoryByPixelAndUser(pixel, user)).thenReturn(visitHistory);
		when(userRepository.getReferenceById(userId)).thenReturn(user);

		// Then
		IndividualHistoryPixelInfoResponse response = pixelService.getIndividualHistoryPixelInfo(pixelId, userId);

		assertEquals(visitHistory.size(), response.getVisitList().size());
		for (int i = 0; i < NUMBER_OF_HISTORY; i++) {
			assertEquals(visitHistory.get(i).getCreatedAt(), response.getVisitList().get(i));
		}
	}

	@Test
	@DisplayName("[getIndividualHistoryPixelInfo] 여러 유저들 중 요청을 보낸 유저만 반환하는지 확인")
	void getIndividualHistoryPixelInfoMultipleUser() {
		final int NUMBER_OF_HISTORY_PER_USER = 2;

		// Given
		Long pixelId = 10000L;
		String address = "은평구";
		int addressNumber = 1;

		Pixel pixel = Pixel.builder()
			.id(pixelId)
			.address(address)
			.addressNumber(addressNumber)
			.build();

		Long userId1 = 1L;
		Long userId2 = 2L;

		User user1 = User.builder()
			.id(userId1)
			.build();

		User user2 = User.builder()
			.id(userId2)
			.build();

		List<PixelUser> visitHistoryUser1 = new ArrayList<>();
		List<PixelUser> visitHistoryUser2 = new ArrayList<>();

		for (int i = 0; i < NUMBER_OF_HISTORY_PER_USER; i++) {
			PixelUser pixelUser1 = PixelUser.builder()
				.user(user1)
				.pixel(pixel)
				.build();
			TestUtils.setCreatedAtOfPixelUser(pixelUser1, LocalDateTime.now().minusSeconds(i));
			visitHistoryUser1.add(pixelUser1);

			PixelUser pixelUser2 = PixelUser.builder()
				.user(user2)
				.pixel(pixel)
				.build();
			TestUtils.setCreatedAtOfPixelUser(pixelUser2, LocalDateTime.now().minusDays(i));
			visitHistoryUser2.add(pixelUser2);
		}

		// When
		when(pixelRepository.findById(pixelId)).thenReturn(Optional.of(pixel));
		when(pixelUserRepository.findAllVisitHistoryByPixelAndUser(pixel, user1)).thenReturn(visitHistoryUser1);
		when(userRepository.getReferenceById(userId1)).thenReturn(user1);

		// Then
		IndividualHistoryPixelInfoResponse response = pixelService.getIndividualHistoryPixelInfo(pixelId, userId1);

		assertEquals(visitHistoryUser1.size(), response.getVisitList().size());
		for (int i = 0; i < NUMBER_OF_HISTORY_PER_USER; i++) {
			assertEquals(visitHistoryUser1.get(i).getCreatedAt(), response.getVisitList().get(i));
		}
	}

	@Test
	@DisplayName("[getIndividualHistoryPixelInfo] 없는 pixelId 를 넣을 경우 PIXEL_NOT_FOUND 에러")
	void getIndividualHistoryPixelInfoNotFound() {
		// Given
		Long pixelId = 1L;
		when(pixelRepository.findById(pixelId)).thenReturn(Optional.empty());

		// When
		AppException exception = assertThrows(AppException.class,
			() -> pixelService.getIndividualModePixelInfo(pixelId));

		// Then
		assertEquals(ErrorCode.PIXEL_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("[getPixelCount] 픽셀 갯수가 정상적으로 불러와지는지 확인")
	void getPixelCountSuccess() {
		// Given
		Long userId = 1L;

		when(pixelRepository.countCurrentPixelByUserId(userId)).thenReturn(3L);
		when(pixelUserRepository.countAccumulatePixelByUserId(userId)).thenReturn(5L);

		// When
		PixelCountResponse pixelCount = pixelService.getPixelCount(userId);

		// Then
		assertEquals(pixelCount.getCurrentPixelCount(), 3L);
		assertEquals(pixelCount.getAccumulatePixelCount(), 5L);
	}

	@Test
	@DisplayName("[occupyPixel] 픽셀을 정상적으로 차지한다.")
	void occupyPixel() {
		// Given
		PixelOccupyRequest pixelOccupyRequest = new PixelOccupyRequest(5L, 78611L, 222L, 233L);
		Pixel pixel = Pixel.builder()
			.x(222L)
			.y(233L)
			.address("대한민국")
			.build();
		when(pixelRepository.findByXAndY(222L, 233L)).thenReturn(Optional.of(pixel));
		// When
		pixelService.occupyPixel(pixelOccupyRequest);
		//Then
		verify(pixelUserRepository, times(1)).save(any());
	}
}
