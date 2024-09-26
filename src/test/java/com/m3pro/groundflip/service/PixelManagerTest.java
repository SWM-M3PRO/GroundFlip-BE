package com.m3pro.groundflip.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

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

import com.m3pro.groundflip.domain.dto.pixel.PixelOccupyRequest;
import com.m3pro.groundflip.domain.dto.pixel.event.PixelAddressUpdateEvent;
import com.m3pro.groundflip.domain.dto.pixel.event.PixelUserInsertEvent;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.repository.PixelRepository;
import com.m3pro.groundflip.repository.PixelUserRepository;

@ExtendWith(MockitoExtension.class)
class PixelManagerTest {
	@Mock
	private PixelRepository pixelRepository;
	@Mock
	private PixelUserRepository pixelUserRepository;
	@Mock
	private ApplicationEventPublisher applicationEventPublisher;
	@Mock
	private RedissonClient redissonClient;
	@Mock
	private UserRankingService userRankingService;
	@Mock
	private CommunityRankingService communityRankingService;
	@InjectMocks
	private PixelManager pixelManager;

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
		when(pixelUserRepository.existsByPixelIdAndUserId(any(), any())).thenReturn(false);
		// When
		pixelManager.occupyPixelWithLock(pixelOccupyRequest);

		//Then
		verify(userRankingService, times(1)).updateCurrentPixelRanking(any(), any());
		verify(userRankingService, times(1)).updateAccumulatedRanking(any());
		verify(communityRankingService, times(1)).updateCurrentPixelRanking(any(), any());
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
		pixelManager.occupyPixelWithLock(pixelOccupyRequest);

		// Then
		verify(applicationEventPublisher, times(1)).publishEvent(any(PixelUserInsertEvent.class));
		verify(userRankingService, times(1)).updateAccumulatedRanking(any());
		verify(communityRankingService, times(1)).updateCurrentPixelRanking(any(), any());
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
		pixelManager.occupyPixelWithLock(pixelOccupyRequest);

		// Then
		verify(applicationEventPublisher, times(1)).publishEvent(any(PixelAddressUpdateEvent.class));
		verify(userRankingService, times(1)).updateAccumulatedRanking(any());
		verify(communityRankingService, times(1)).updateCurrentPixelRanking(any(), any());
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
		pixelManager.occupyPixelWithLock(pixelOccupyRequest);

		// Then
		verify(applicationEventPublisher, times(0)).publishEvent(any(PixelAddressUpdateEvent.class));
		verify(userRankingService, times(1)).updateAccumulatedRanking(any());
		verify(communityRankingService, times(1)).updateCurrentPixelRanking(any(), any());
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
