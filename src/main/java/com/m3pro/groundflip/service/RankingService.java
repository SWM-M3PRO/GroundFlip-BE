package com.m3pro.groundflip.service;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.repository.RankingRedisRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {
	private final RankingRedisRepository rankingRedisRepository;

	public void increaseCurrentPixelCount(Long userId) {
		rankingRedisRepository.increaseCurrentPixelCount(userId);
	}

	public void decreaseCurrentPixelCount(Long userId) {
		rankingRedisRepository.decreaseCurrentPixelCount(userId);
	}

	public void updateRankingAfterOccupy(Long occupyingUserId, Long deprivedUserId) {
		increaseCurrentPixelCount(occupyingUserId);
		decreaseCurrentPixelCount(deprivedUserId);
	}

	public Long getCurrentPixelCount(Long userId) {
		return rankingRedisRepository.getUserCurrentPixelCount(userId).orElse(0L);
	}
}
