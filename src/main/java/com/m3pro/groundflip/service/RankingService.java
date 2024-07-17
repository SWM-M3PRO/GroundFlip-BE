package com.m3pro.groundflip.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.ranking.Ranking;
import com.m3pro.groundflip.domain.dto.ranking.UserRankingResponse;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.RankingRedisRepository;
import com.m3pro.groundflip.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {
	private final RankingRedisRepository rankingRedisRepository;
	private final UserRepository userRepository;

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

	public List<UserRankingResponse> getAllUserRanking() {
		List<Ranking> rankings = rankingRedisRepository.getRankingsWithCurrentPixelCount();

		return rankings.stream()
			.map(ranking -> {
				User user = userRepository.findById(ranking.getUserId())
					.orElseThrow(() -> new RuntimeException("User not found"));
				return UserRankingResponse.from(user, ranking.getRank(), ranking.getCurrentPixelCount());
			})
			.collect(Collectors.toList());
	}

	public UserRankingResponse getUserRankInfo(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		Long rank = getUserRank(userId);
		Long currentPixelCount = getCurrentPixelCount(userId);
		return UserRankingResponse.from(user, rank, currentPixelCount);
	}

	private Long getUserRank(Long userId) {
		return rankingRedisRepository.getUserRank(userId)
			.orElseThrow(() -> {
				log.error("User {} not register at redis", userId);
				return new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
			});
	}
}
