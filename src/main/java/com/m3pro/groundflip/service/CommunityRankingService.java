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

import com.m3pro.groundflip.domain.dto.ranking.CommunityRankingResponse;
import com.m3pro.groundflip.domain.dto.ranking.Ranking;
import com.m3pro.groundflip.domain.entity.Community;
import com.m3pro.groundflip.domain.entity.CommunityRankingHistory;
import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.CommunityRankingHistoryRepository;
import com.m3pro.groundflip.repository.CommunityRankingRedisRepository;
import com.m3pro.groundflip.repository.CommunityRepository;
import com.m3pro.groundflip.util.DateUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityRankingService {
	private final CommunityRankingRedisRepository communityRankingRedisRepository;
	private final CommunityRepository communityRepository;
	private final CommunityRankingHistoryRepository communityRankingHistoryRepository;

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

	public void updateCurrentPixelRankingAfterOccupy(Long occupyingCommunityId, Long deprivedCommunityId) {
		communityRankingRedisRepository.increaseCurrentPixelCount(occupyingCommunityId);
		communityRankingRedisRepository.decreaseCurrentPixelCount(deprivedCommunityId);
	}

	public void updateAccumulatedRanking(Long occupyingCommunityId) {
		communityRankingRedisRepository.increaseAccumulatePixelCount(occupyingCommunityId);
	}

	public Long getCurrentPixelCountFromCache(Long communityId) {
		return communityRankingRedisRepository.getCurrentPixelCount(communityId).orElse(0L);
	}

	public Long getAccumulatePixelCount(Long communityId) {
		return communityRankingRedisRepository.getAccumulatePixelCount(communityId).orElse(0L);
	}

	public List<CommunityRankingResponse> getCurrentPixelAllUCommunityRankings(LocalDate lookUpDate) {
		if (lookUpDate == null) {
			lookUpDate = LocalDate.now();
		}

		if (DateUtils.isDateInCurrentWeek(lookUpDate)) {
			return getCurrentWeekCurrentPixelRankings();
		} else {
			return getPastWeekCurrentPixelRankingsByDate(lookUpDate);
		}
	}

	private List<CommunityRankingResponse> getPastWeekCurrentPixelRankingsByDate(LocalDate lookUpDate) {
		return communityRankingHistoryRepository.findAllByYearAndWeek(
			lookUpDate.getYear(),
			DateUtils.getWeekOfDate(lookUpDate)
		);
	}

	private List<CommunityRankingResponse> getCurrentWeekCurrentPixelRankings() {
		List<Ranking> rankings = communityRankingRedisRepository.getRankingsWithCurrentPixelCount();
		Map<Long, Community> communities = getRankedCommunities(rankings);

		rankings = filterNotExistCommunities(rankings, communities);

		return rankings.stream()
			.map(ranking -> {
				Community community = communities.get(ranking.getId());
				return CommunityRankingResponse.from(community, ranking.getRank(), ranking.getCurrentPixelCount());
			})
			.collect(Collectors.toList());
	}

	private List<Ranking> filterNotExistCommunities(List<Ranking> rankings, Map<Long, Community> communities) {
		return rankings.stream()
			.filter(ranking -> {
				if (communities.containsKey(ranking.getId())) {
					return true;
				} else {
					log.error("[filterNotExistUsers] communityId {}은 데이터베이스에 존재하지 않음", ranking.getId());
					return false;
				}
			})
			.toList();
	}

	/**
	 * 30위 안에 있는 그룹 entity 를 반환한다.
	 * @param rankings 랭킹 정보 (그룹 id 와 rank, 점수만 들어있음)
	 * @return Map 형식의 Community 엔티티
	 */
	private Map<Long, Community> getRankedCommunities(List<Ranking> rankings) {
		Set<Long> communityIds = rankings.stream()
			.map(Ranking::getId)
			.collect(Collectors.toSet());

		List<Community> communities = communityRepository.findAllById(communityIds);

		return communities.stream()
			.collect(Collectors.toMap(Community::getId, community -> community));
	}

	/**
	 * 그룹의 순위 정보를 반환한다.
	 * @param communityId 그룹 Id
	 * @return 그룹의 순위 정보
	 */
	public CommunityRankingResponse getCommunityCurrentPixelRankInfo(Long communityId, LocalDate lookUpDate) {
		if (lookUpDate == null) {
			lookUpDate = LocalDate.now();
		}

		if (DateUtils.isDateInCurrentWeek(lookUpDate)) {
			return getCurrentWeekCurrentPixelCommunityRanking(communityId);
		} else {
			return getPastWeekCurrentPixelCommunityRanking(communityId, lookUpDate);
		}
	}

	private CommunityRankingResponse getPastWeekCurrentPixelCommunityRanking(Long communityId, LocalDate lookUpDate) {
		Community community = communityRepository.findById(communityId)
			.orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));

		Optional<CommunityRankingHistory> rankingHistory = communityRankingHistoryRepository.findByCommunityIdAndYearAndWeek(
			communityId,
			lookUpDate.getYear(),
			DateUtils.getWeekOfDate(lookUpDate)
		);

		if (rankingHistory.isPresent() && rankingHistory.get().getCurrentPixelCount() > 0) {
			return CommunityRankingResponse.from(
				community,
				rankingHistory.get().getRanking(),
				rankingHistory.get().getCurrentPixelCount());
		} else {
			return CommunityRankingResponse.from(community);
		}
	}

	private CommunityRankingResponse getCurrentWeekCurrentPixelCommunityRanking(Long communityId) {
		Community community = communityRepository.findById(communityId)
			.orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));
		Long currentPixelCount = getCurrentPixelCountFromCache(communityId);

		if (currentPixelCount == 0) {
			return CommunityRankingResponse.from(community, null, null);
		} else {
			Long rank = getCommunityCurrentPixelRankFromCache(communityId);
			return CommunityRankingResponse.from(community, rank, currentPixelCount);
		}
	}

	/**
	 * 그룹의 순위를 반환한다
	 * @param communityId 그룹 Id
	 * @return 그룹의 순위
	 */
	public Long getCommunityCurrentPixelRankFromCache(Long communityId) {
		return communityRankingRedisRepository.getCurrentPixelRank(communityId)
			.orElseThrow(() -> {
				log.error("Community {} not register at redis", communityId);
				return new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
			});
	}
}
