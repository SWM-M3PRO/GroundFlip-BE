package com.m3pro.groundflip.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.ranking.Ranking;
import com.m3pro.groundflip.domain.dto.ranking.UserRankingResponse;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.domain.entity.RankingHistory;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.RankingHistoryRepository;
import com.m3pro.groundflip.repository.RankingRedisRepository;
import com.m3pro.groundflip.repository.UserRepository;
import com.m3pro.groundflip.util.DateUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {
	private final RankingRedisRepository rankingRedisRepository;
	private final UserRepository userRepository;
	private final RankingHistoryRepository rankingHistoryRepository;

	public void updateRanking(Pixel targetPixel, Long occupyingUserId) {
		Long originalOwnerUserId = targetPixel.getUserId();
		LocalDateTime thisWeekStart = DateUtils.getThisWeekStartDate().atTime(0, 0);
		LocalDateTime modifiedAt = targetPixel.getModifiedAt();

		if (Objects.equals(originalOwnerUserId, occupyingUserId)) {
			if (modifiedAt.isAfter(thisWeekStart)) {
				return;
			}
			increaseCurrentPixelCount(occupyingUserId);
		} else {
			if (originalOwnerUserId == null || modifiedAt.isBefore(thisWeekStart)) {
				increaseCurrentPixelCount(occupyingUserId);
			} else {
				updateRankingAfterOccupy(occupyingUserId, originalOwnerUserId);
			}
		}
	}

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
	public Long getCurrentPixelCountFromCache(Long userId) {
		return rankingRedisRepository.getUserCurrentPixelCount(userId).orElse(0L);
	}

	/**
	 * 모든 유저의 순위를 반환한다. 최대 30개
	 * @return 모든 유저의 순위
	 */
	public List<UserRankingResponse> getAllUserRankings(LocalDate lookUpDate) {
		if (lookUpDate == null) {
			lookUpDate = LocalDate.now();
		}

		if (DateUtils.isDateInCurrentWeek(lookUpDate)) {
			return getCurrentWeekRankings();
		} else {
			return getPastWeekRankingsByDate(lookUpDate);
		}
	}

	private List<UserRankingResponse> getPastWeekRankingsByDate(LocalDate lookUpDate) {
		return rankingHistoryRepository.findAllByYearAndWeek(
			lookUpDate.getYear(),
			DateUtils.getWeekOfDate(lookUpDate)
		);
	}

	private List<UserRankingResponse> getCurrentWeekRankings() {
		List<Ranking> rankings = rankingRedisRepository.getRankingsWithCurrentPixelCount();
		Map<Long, User> users = getRankedUsers(rankings);

		rankings = filterNotExistUsers(rankings, users);

		return rankings.stream()
			.map(ranking -> {
				User user = users.get(ranking.getUserId());
				return UserRankingResponse.from(user, ranking.getRank(), ranking.getCurrentPixelCount());
			})
			.collect(Collectors.toList());
	}

	private List<Ranking> filterNotExistUsers(List<Ranking> rankings, Map<Long, User> users) {
		return rankings.stream()
			.filter(ranking -> {
				if (users.containsKey(ranking.getUserId())) {
					return true;
				} else {
					log.error("[filterNotExistUsers] userId {}은 데이터베이스에 존재하지 않음", ranking.getUserId());
					return false;
				}
			})
			.toList();
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
	public UserRankingResponse getUserRankInfo(Long userId, LocalDate lookUpDate) {
		if (lookUpDate == null) {
			lookUpDate = LocalDate.now();
		}

		if (DateUtils.isDateInCurrentWeek(lookUpDate)) {
			return getCurrentWeekUserRanking(userId);
		} else {
			return getPastWeekUserRanking(userId, lookUpDate);
		}
	}

	private UserRankingResponse getPastWeekUserRanking(Long userId, LocalDate lookUpDate) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

		Optional<RankingHistory> rankingHistory = rankingHistoryRepository.findByUserIdAndYearAndWeek(
			userId,
			lookUpDate.getYear(),
			DateUtils.getWeekOfDate(lookUpDate)
		);

		if (rankingHistory.isPresent()) {
			return UserRankingResponse.from(
				user,
				rankingHistory.get().getRanking(),
				rankingHistory.get().getCurrentPixelCount());
		} else {
			return UserRankingResponse.from(user);
		}
	}

	private UserRankingResponse getCurrentWeekUserRanking(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		Long rank = getUserRankFromCache(userId);
		Long currentPixelCount = getCurrentPixelCountFromCache(userId);
		return UserRankingResponse.from(user, rank, currentPixelCount);
	}

	/**
	 * 유저의 순위를 반환한다
	 * @param userId 사용자 Id
	 * @return 사용자의 순위
	 */
	private Long getUserRankFromCache(Long userId) {
		return rankingRedisRepository.getUserRank(userId)
			.orElseThrow(() -> {
				log.error("User {} not register at redis", userId);
				return new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
			});
	}
}
