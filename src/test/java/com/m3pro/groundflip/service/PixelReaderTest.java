package com.m3pro.groundflip.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
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

import com.m3pro.groundflip.domain.dto.pixel.CommunityPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixel.IndividualPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixel.PixelCountResponse;
import com.m3pro.groundflip.domain.dto.pixelUser.IndividualHistoryPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixelUser.PixelOwnerUser;
import com.m3pro.groundflip.domain.dto.pixelUser.VisitedUser;
import com.m3pro.groundflip.domain.entity.Community;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.domain.entity.PixelUser;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.CommunityRepository;
import com.m3pro.groundflip.repository.DailyPixelRepository;
import com.m3pro.groundflip.repository.PixelRepository;
import com.m3pro.groundflip.repository.PixelUserRepository;
import com.m3pro.groundflip.repository.UserRepository;
import com.m3pro.groundflip.util.TestUtils;

@ExtendWith(MockitoExtension.class)
public class PixelReaderTest {
	@Mock
	private PixelRepository pixelRepository;
	@Mock
	private PixelUserRepository pixelUserRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private UserRankingService userRankingService;
	@Mock
	private CommunityRankingService communityRankingService;
	@Mock
	private CommunityRepository communityRepository;
	@Mock
	private DailyPixelRepository dailyPixelRepository;
	@InjectMocks
	private PixelReader pixelReader;

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
			() -> pixelReader.getIndividualModePixelInfo(pixelId));

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
		when(pixelUserRepository.countAccumulatePixelByUserId(ownerId,
			LocalDate.parse("2024-07-15").atStartOfDay())).thenReturn(10L);
		when(userRankingService.getCurrentPixelCountFromCache(ownerId)).thenReturn(5L);

		// When
		IndividualPixelInfoResponse response = pixelReader.getIndividualModePixelInfo(pixelId);

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
		IndividualPixelInfoResponse response = pixelReader.getIndividualModePixelInfo(pixelId);

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
		when(pixelUserRepository.findAllVisitHistoryByPixelAndUser(pixel, user,
			LocalDate.parse("2024-07-15").atStartOfDay())).thenReturn(visitHistory);
		when(userRepository.getReferenceById(userId)).thenReturn(user);

		// Then
		IndividualHistoryPixelInfoResponse response = pixelReader.getIndividualHistoryPixelInfo(pixelId, userId, null);

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
		String address = "서울특별시 은평구 녹번동";
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
		when(pixelUserRepository.findAllVisitHistoryByPixelAndUser(pixel, user1,
			LocalDate.parse("2024-07-15").atStartOfDay())).thenReturn(visitHistoryUser1);
		when(userRepository.getReferenceById(userId1)).thenReturn(user1);

		// Then
		IndividualHistoryPixelInfoResponse response = pixelReader.getIndividualHistoryPixelInfo(pixelId, userId1,
			null);

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
			() -> pixelReader.getIndividualModePixelInfo(pixelId));

		// Then
		assertEquals(ErrorCode.PIXEL_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("[getPixelCount] 픽셀 갯수가 정상적으로 불러와지는지 확인")
	void getPixelCountSuccess() {
		// Given
		Long userId = 1L;

		when(userRankingService.getCurrentPixelCountFromCache(userId)).thenReturn(3L);
		when(userRankingService.getAccumulatePixelCount(any())).thenReturn(5L);

		// When
		PixelCountResponse pixelCount = pixelReader.getPixelCount(userId, null);

		// Then
		assertEquals(pixelCount.getCurrentPixelCount(), 3L);
		assertEquals(pixelCount.getAccumulatePixelCount(), 5L);
	}

	@Test
	@DisplayName("[getPixelCount] lookup-date 가 있을 떄 픽셀 갯수가 정상적으로 불러와지는지 확인")
	void getPixelCountLookupDate() {
		// Given
		Long userId = 1L;

		when(userRankingService.getCurrentPixelCountFromCache(userId)).thenReturn(3L);
		when(pixelUserRepository.countAccumulatePixelByUserId(userId,
			LocalDate.parse("2024-07-15").atStartOfDay())).thenReturn(5L);

		// When
		PixelCountResponse pixelCount = pixelReader.getPixelCount(userId, LocalDate.parse("2024-07-15"));

		// Then
		assertEquals(pixelCount.getCurrentPixelCount(), 3L);
		assertEquals(pixelCount.getAccumulatePixelCount(), 5L);
	}

	@Test
	@DisplayName("[getCommunityPixelCount] 그룹의 픽셀 개수를 반환")
	void getCommunityPixelCountTest() {
		Long communityId = 1L;
		when(communityRankingService.getCurrentPixelCountFromCache(communityId)).thenReturn(3L);
		PixelCountResponse pixelCountResponse = pixelReader.getCommunityPixelCount(communityId);
		assertEquals(pixelCountResponse.getCurrentPixelCount(), 3L);
	}

	@Test
	@DisplayName("[getCommunityModePixelInfo] 그룹 모드의 픽셀 정보를 반환한다")
	void getCommunityModePixelInfoTest() {
		Long pixelId = 1L;
		Long communityId = 2L;
		Pixel pixel = Pixel.builder().id(pixelId).communityId(communityId).build();
		Community community = Community.builder().id(communityId).name("test").build();
		when(pixelRepository.findById(pixelId)).thenReturn(Optional.of(pixel));
		when(communityRankingService.getAccumulatePixelCount(communityId)).thenReturn(3L);
		when(communityRankingService.getCurrentPixelCountFromCache(communityId)).thenReturn(3L);
		when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));

		CommunityPixelInfoResponse communityPixelInfoResponse = pixelReader.getCommunityModePixelInfo(pixelId);

		assertThat(communityPixelInfoResponse.getPixelOwnerCommunity().getCommunityId()).isEqualTo(communityId);
	}

	@Test
	@DisplayName("[getDailyPixel] 그룹 모드의 픽셀 정보를 반환한다")
	void getDailyPixelTest() {
		Long userId = 1L;
		LocalDate startDate = LocalDate.parse("2024-07-15");
		LocalDate endDate = LocalDate.parse("2024-07-18");
		pixelReader.getDailyPixel(userId, startDate, endDate);
		verify(dailyPixelRepository).findAllDailyPixel(userId, startDate, endDate);
	}
}
