package com.m3pro.groundflip.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.dto.ranking.UserRankingResponse;
import com.m3pro.groundflip.domain.entity.RankingHistory;

public interface RankingHistoryRepository extends JpaRepository<RankingHistory, Long> {
	@Query("""
			SELECT new com.m3pro.groundflip.domain.dto.ranking.UserRankingResponse
			(u.id, u.nickname, u.profileImage, rh.currentPixelCount, rh.ranking)
			FROM RankingHistory rh 
			INNER JOIN User u on u.id = rh.userId 
			WHERE rh.year = :requestYear AND rh.week = :requestWeek AND rh.currentPixelCount > 0
			ORDER BY rh.ranking ASC 
			LIMIT 100 
		""")
	List<UserRankingResponse> findAllByYearAndWeek(@Param("requestYear") int year, @Param("requestWeek") int week);

	@Query("""
			SELECT new com.m3pro.groundflip.domain.dto.ranking.UserRankingResponse
			(u.id, u.nickname, u.profileImage, rh.accumulatePixelCount, rh.accumulateRanking)
			FROM RankingHistory rh 
			INNER JOIN User u on u.id = rh.userId 
			WHERE rh.year = :requestYear AND rh.week = :requestWeek AND rh.accumulatePixelCount > 0
			ORDER BY rh.ranking ASC 
			LIMIT 100 
		""")
	List<UserRankingResponse> findAllAccumulateRankingByYearAndWeek(
		@Param("requestYear") int year,
		@Param("requestWeek") int week);

	Optional<RankingHistory> findByUserIdAndYearAndWeek(
		@Param("userId") Long userId,
		@Param("requestYear") int year,
		@Param("requestWeek") int week);

	void deleteByUserIdAndYearAndWeek(Long userId, int year, int week);
}