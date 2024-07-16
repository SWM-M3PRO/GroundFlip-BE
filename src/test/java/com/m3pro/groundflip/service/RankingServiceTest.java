package com.m3pro.groundflip.service;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.repository.RankingRedisRepository;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {
	@Mock
	private RankingRedisRepository rankingRedisRepository;
	@InjectMocks
	private RankingService rankingService;

	@BeforeEach
	void init() {
		reset(rankingRedisRepository);
	}

	@Test
	@DisplayName("[increaseCurrentPixelCount] userId 에 해당하는 현재 소유 픽셀의 개수를 1 증가시킨다.")
	void increaseCurrentPixelCountTest() {
		Long userId = 1L;

		rankingService.increaseCurrentPixelCount(userId);

		verify(rankingRedisRepository, times(1)).increaseCurrentPixelCount(userId);
	}

	@Test
	@DisplayName("[decreasePixelCount] userId 에 해당하는 현재 소유 픽셀의 개수를 1 감소시킨다.")
	void decreasePixelCountTest() {
		Long userId = 1L;

		rankingService.decreaseCurrentPixelCount(userId);

		verify(rankingRedisRepository, times(1)).decreaseCurrentPixelCount(userId);
	}

	@Test
	@DisplayName("[decreasePixelCount] occupyingUserId 에 해당하는 현재 소유 픽셀의 개수를 1 증가시키고 deprivedUserId 에 해당하는 현재 소유 픽셀의 개수를 1 감소시킨다.")
	void updateRankingAfterOccupyTest() {
		Long occupyingUserId = 1L;
		Long deprivedUserId = 2L;

		rankingService.updateRankingAfterOccupy(occupyingUserId, deprivedUserId);

		verify(rankingRedisRepository, times(1)).decreaseCurrentPixelCount(deprivedUserId);
		verify(rankingRedisRepository, times(1)).increaseCurrentPixelCount(occupyingUserId);
	}
}