package com.m3pro.groundflip.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;

import com.m3pro.groundflip.domain.dto.pixel.IndividualPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixel.PixelCountResponse;
import com.m3pro.groundflip.domain.dto.pixel.PixelOccupyRequest;
import com.m3pro.groundflip.domain.dto.pixel.event.PixelAddressUpdateEvent;
import com.m3pro.groundflip.domain.dto.pixel.event.PixelUserInsertEvent;
import com.m3pro.groundflip.domain.dto.pixelUser.IndividualHistoryPixelInfoResponse;
import com.m3pro.groundflip.domain.dto.pixelUser.PixelOwnerUser;
import com.m3pro.groundflip.domain.dto.pixelUser.VisitedUser;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.domain.entity.PixelUser;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
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
	private RankingService rankingService;
	@Mock
	private ApplicationEventPublisher applicationEventPublisher;
	@Mock
	private RedissonClient redissonClient;
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
		when(pixelUserRepository.countAccumulatePixelByUserId(ownerId,
			LocalDate.parse("2024-07-15").atStartOfDay())).thenReturn(10L);
		when(rankingService.getCurrentPixelCountFromCache(ownerId)).thenReturn(5L);

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
		when(pixelUserRepository.findAllVisitHistoryByPixelAndUser(pixel, user,
			LocalDate.parse("2024-07-15").atStartOfDay())).thenReturn(visitHistory);
		when(userRepository.getReferenceById(userId)).thenReturn(user);

		// Then
		IndividualHistoryPixelInfoResponse response = pixelService.getIndividualHistoryPixelInfo(pixelId, userId, null);

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
		IndividualHistoryPixelInfoResponse response = pixelService.getIndividualHistoryPixelInfo(pixelId, userId1,
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
			() -> pixelService.getIndividualModePixelInfo(pixelId));

		// Then
		assertEquals(ErrorCode.PIXEL_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("[getPixelCount] 픽셀 갯수가 정상적으로 불러와지는지 확인")
	void getPixelCountSuccess() {
		// Given
		Long userId = 1L;

		when(rankingService.getCurrentPixelCountFromCache(userId)).thenReturn(3L);
		when(pixelUserRepository.countAccumulatePixelByUserId(userId,
			LocalDate.parse("2024-07-15").atStartOfDay())).thenReturn(5L);

		// When
		PixelCountResponse pixelCount = pixelService.getPixelCount(userId, null);

		// Then
		assertEquals(pixelCount.getCurrentPixelCount(), 3L);
		assertEquals(pixelCount.getAccumulatePixelCount(), 5L);
	}

	@Test
	@DisplayName("[occupyPixel] 픽셀을 정상적으로 차지하여 타겟 픽셀의 userId가 바뀐다.")
	void occupyPixel() {
		// Given
		PixelOccupyRequest pixelOccupyRequest = new PixelOccupyRequest(5L, 78611L, 222L, 233L);
		Pixel pixel = Pixel.builder()
			.x(222L)
			.y(233L)
			.userId(1L)
			.address("대한민국")
			.build();
		when(pixelRepository.findByXAndY(222L, 233L)).thenReturn(Optional.of(pixel));
		when(redissonClient.getLock(any())).thenReturn(new RedissonLock());
		// When
		pixelService.occupyPixelWithLock(pixelOccupyRequest);

		//Then
		assertEquals(5L, pixel.getUserId());
	}

	@Test
	@DisplayName("[occupyPixel] 픽셀을 차지할 때 PixelUserInsertEvent가 발행되는지 확인")
	void pixelUserInsertEventPublish() {
		PixelOccupyRequest pixelOccupyRequest = new PixelOccupyRequest(5L, 78611L, 222L, 233L);
		Pixel pixel = Pixel.builder()
			.x(222L)
			.y(233L)
			.userId(1L)
			.address("대한민국")
			.build();
		when(pixelRepository.findByXAndY(222L, 233L)).thenReturn(Optional.of(pixel));
		when(redissonClient.getLock(any())).thenReturn(new RedissonLock());
		// When
		pixelService.occupyPixelWithLock(pixelOccupyRequest);

		// Then
		verify(applicationEventPublisher, times(1)).publishEvent(any(PixelUserInsertEvent.class));
	}

	@Test
	@DisplayName("[occupyPixel] 픽셀을 차지할 때 주소가 null이면 PixelAddressUpdate 이벤트가 발행되는지 확인")
	void pixelAddressUpdateEventPublish() {
		PixelOccupyRequest pixelOccupyRequest = new PixelOccupyRequest(5L, 78611L, 222L, 233L);
		Pixel pixel = Pixel.builder()
			.x(222L)
			.y(233L)
			.userId(1L)
			.address(null)
			.build();
		when(pixelRepository.findByXAndY(222L, 233L)).thenReturn(Optional.of(pixel));
		when(redissonClient.getLock(any())).thenReturn(new RedissonLock());
		// When
		pixelService.occupyPixelWithLock(pixelOccupyRequest);

		// Then
		verify(applicationEventPublisher, times(1)).publishEvent(any(PixelAddressUpdateEvent.class));
	}

	@Test
	@DisplayName("[occupyPixel] 픽셀을 차지할 때 주소가 null이 아니라면 PixelAddressUpdate 이벤트가 발행되지 않는지 확인")
	void pixelAddressUpdateEventNotPublish() {
		PixelOccupyRequest pixelOccupyRequest = new PixelOccupyRequest(5L, 78611L, 222L, 233L);
		Pixel pixel = Pixel.builder()
			.x(222L)
			.y(233L)
			.userId(1L)
			.address("대한민국 ")
			.build();
		when(pixelRepository.findByXAndY(222L, 233L)).thenReturn(Optional.of(pixel));
		when(redissonClient.getLock(any())).thenReturn(new RedissonLock());
		// When
		pixelService.occupyPixelWithLock(pixelOccupyRequest);

		// Then
		verify(applicationEventPublisher, times(0)).publishEvent(any(PixelAddressUpdateEvent.class));
	}

	static class RedissonLock implements RLock {
		@Override
		public String getName() {
			return "";
		}

		@Override
		public void lockInterruptibly(long l, TimeUnit timeUnit) throws InterruptedException {

		}

		@Override
		public boolean tryLock(long l, long l1, TimeUnit timeUnit) throws InterruptedException {
			return true;
		}

		@Override
		public void lock(long l, TimeUnit timeUnit) {

		}

		@Override
		public boolean forceUnlock() {
			return false;
		}

		@Override
		public boolean isLocked() {
			return false;
		}

		@Override
		public boolean isHeldByThread(long l) {
			return false;
		}

		@Override
		public boolean isHeldByCurrentThread() {
			return false;
		}

		@Override
		public int getHoldCount() {
			return 0;
		}

		@Override
		public long remainTimeToLive() {
			return 0;
		}

		@Override
		public void lock() {

		}

		@Override
		public void lockInterruptibly() throws InterruptedException {

		}

		@Override
		public boolean tryLock() {
			return false;
		}

		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
			return false;
		}

		@Override
		public void unlock() {

		}

		@Override
		public Condition newCondition() {
			return null;
		}

		@Override
		public RFuture<Boolean> forceUnlockAsync() {
			return null;
		}

		@Override
		public RFuture<Void> unlockAsync() {
			return null;
		}

		@Override
		public RFuture<Void> unlockAsync(long l) {
			return null;
		}

		@Override
		public RFuture<Boolean> tryLockAsync() {
			return null;
		}

		@Override
		public RFuture<Void> lockAsync() {
			return null;
		}

		@Override
		public RFuture<Void> lockAsync(long l) {
			return null;
		}

		@Override
		public RFuture<Void> lockAsync(long l, TimeUnit timeUnit) {
			return null;
		}

		@Override
		public RFuture<Void> lockAsync(long l, TimeUnit timeUnit, long l1) {
			return null;
		}

		@Override
		public RFuture<Boolean> tryLockAsync(long l) {
			return null;
		}

		@Override
		public RFuture<Boolean> tryLockAsync(long l, TimeUnit timeUnit) {
			return null;
		}

		@Override
		public RFuture<Boolean> tryLockAsync(long l, long l1, TimeUnit timeUnit) {
			return null;
		}

		@Override
		public RFuture<Boolean> tryLockAsync(long l, long l1, TimeUnit timeUnit, long l2) {
			return null;
		}

		@Override
		public RFuture<Integer> getHoldCountAsync() {
			return null;
		}

		@Override
		public RFuture<Boolean> isLockedAsync() {
			return null;
		}

		@Override
		public RFuture<Long> remainTimeToLiveAsync() {
			return null;
		}
	}
}
