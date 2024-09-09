package com.m3pro.groundflip.service;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.repository.CommunityRankingRedisRepository;
import com.m3pro.groundflip.util.DateUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityRankingService {
	private final CommunityRankingRedisRepository communityRankingRedisRepository;

	public void updateCurrentPixelRanking(Pixel targetPixel, Long occupyingCommunityId) {
		Long originalOwnerCommunityId = targetPixel.getCommunityId();

		LocalDateTime thisWeekStart = DateUtils.getThisWeekStartDate().atTime(0, 0);
		LocalDateTime communityOccupiedAt = targetPixel.getCommunityOccupiedAt();

		if (Objects.equals(originalOwnerCommunityId, occupyingCommunityId)) {
			if (communityOccupiedAt.isAfter(thisWeekStart)) {
				return;
			}
			communityRankingRedisRepository.increaseCurrentPixelCount(occupyingCommunityId);
		} else {
			if (originalOwnerCommunityId == null || communityOccupiedAt.isBefore(thisWeekStart)) {
				communityRankingRedisRepository.increaseCurrentPixelCount(occupyingCommunityId);
			} else {
				updateCurrentPixelRankingAfterOccupy(occupyingCommunityId, originalOwnerCommunityId);
			}
		}
	}

	private void updateCurrentPixelRankingAfterOccupy(Long occupyingCommunityId, Long deprivedCommunityId) {
		communityRankingRedisRepository.increaseCurrentPixelCount(occupyingCommunityId);
		communityRankingRedisRepository.decreaseCurrentPixelCount(deprivedCommunityId);
	}

	public void updateAccumulatedRanking(Long occupyingCommunityId) {
		communityRankingRedisRepository.increaseAccumulatePixelCount(occupyingCommunityId);
	}
}
