package com.m3pro.groundflip.service;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.pixel.PixelOccupyRequest;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PixelManagerWithLock {
	private static final String REDISSON_LOCK_PREFIX = "LOCK:";
	private final PixelManager pixelManager;
	private final RedissonClient redissonClient;

	public void occupyPixelWithLock(PixelOccupyRequest pixelOccupyRequest) {
		String lockName = REDISSON_LOCK_PREFIX + pixelOccupyRequest.getX() + pixelOccupyRequest.getY();
		RLock rLock = redissonClient.getLock(lockName);

		long waitTime = 5L;
		long leaseTime = 3L;
		TimeUnit timeUnit = TimeUnit.SECONDS;
		try {
			boolean available = rLock.tryLock(waitTime, leaseTime, timeUnit);
			if (!available) {
				throw new AppException(ErrorCode.LOCK_ACQUISITION_ERROR);
			}

			pixelManager.occupyPixel(pixelOccupyRequest);

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			rLock.unlock();
		}
	}
}
