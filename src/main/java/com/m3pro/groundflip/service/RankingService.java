package com.m3pro.groundflip.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

	/**
	 * 현재 픽셀의 수를 1 증가 시킨다.
	 * @param userId 사용자 id
	 */
	public void increaseCurrentPixelCount(Long userId) {
		rankingRedisRepository.increaseCurrentPixelCount(userId);
	}

	/**
	 * 현재 픽셀의 수를 1 감소 시킨다.
	 * @param userId 사용자 id
	 */
	public void decreaseCurrentPixelCount(Long userId) {
		rankingRedisRepository.decreaseCurrentPixelCount(userId);
	}

	/**
	 * 픽셀을 새로 차지하는 유저의 점수는 1증가, 빼앗긴 유저의 점수는 1감소
	 * @param occupyingUserId 픽셀을 새로 차지하는 유저
	 * @param deprivedUserId 픽셀을 뺴앗긴 유저
	 */
	public void updateRankingAfterOccupy(Long occupyingUserId, Long deprivedUserId) {
		increaseCurrentPixelCount(occupyingUserId);
		decreaseCurrentPixelCount(deprivedUserId);
	}

	/**
	 * 현재 소유한 픽셀의 개수를 반환한다.
	 * @param userId 사용자 id
	 * @return 현재 소유한 픽셀의 개수
	 */
	public Long getCurrentPixelCount(Long userId) {
		return rankingRedisRepository.getUserCurrentPixelCount(userId).orElse(0L);
	}

	/**
	 * 모든 유저의 순위를 반환한다. 최대 30개
	 * @return 모든 유저의 순위
	 */
	public List<UserRankingResponse> getAllUserRanking() {
		List<Ranking> rankings = rankingRedisRepository.getRankingsWithCurrentPixelCount();
		Map<Long, User> users = getRankedUsers(rankings);

		return rankings.stream()
			.map(ranking -> {
				User user = users.get(ranking.getUserId());
				if (user == null) {
					log.error("User {} Not Register At Redis Sorted Set", ranking.getUserId());
					throw new RuntimeException("User not found");
				}
				return UserRankingResponse.from(user, ranking.getRank(), ranking.getCurrentPixelCount());
			})
			.collect(Collectors.toList());
	}

	/**
	 * 30위 안에 있는 유저 entity 를 반환한다.
	 * @param rankings 랭킹 정보 (유저 id 와 rank, 점수만 들어있음)
	 * @return Map 형식의 User 엔티티
	 */
	private Map<Long, User> getRankedUsers(List<Ranking> rankings) {
		Set<Long> userIds = rankings.stream()
			.map(Ranking::getUserId)
			.collect(Collectors.toSet());
		List<User> users = userRepository.findAllById(userIds);
		return users.stream()
			.collect(Collectors.toMap(User::getId, user -> user));
	}

	/**
	 * 유저의 순위 정보를 반환한다.
	 * @param userId 사용자 Id
	 * @return 유저의 순위 정보
	 */
	public UserRankingResponse getUserRankInfo(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		Long rank = getUserRank(userId);
		Long currentPixelCount = getCurrentPixelCount(userId);
		return UserRankingResponse.from(user, rank, currentPixelCount);
	}

	/**
	 * 유저의 순위를 반환한다
	 * @param userId 사용자 Id
	 * @return 사용자의 순위
	 */
	private Long getUserRank(Long userId) {
		return rankingRedisRepository.getUserRank(userId)
			.orElseThrow(() -> {
				log.error("User {} not register at redis", userId);
				return new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
			});
	}
}
