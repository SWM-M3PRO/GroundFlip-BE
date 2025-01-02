package com.m3pro.groundflip.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.dto.ranking.CommunityRankingResponse;
import com.m3pro.groundflip.domain.entity.CommunityRankingHistory;

public interface CommunityRankingHistoryRepository extends JpaRepository<CommunityRankingHistory, Long> {
	@Query("""
			SELECT new com.m3pro.groundflip.domain.dto.ranking.CommunityRankingResponse
			(c.id, c.name, c.backgroundImageUrl, crh.currentPixelCount, crh.ranking)
			FROM CommunityRankingHistory crh 
			INNER JOIN Community c on c.id = crh.communityId 
			WHERE crh.year = :requestYear AND crh.week = :requestWeek AND crh.currentPixelCount > 0
			ORDER BY crh.ranking ASC 
			LIMIT 30 
		""")
	List<CommunityRankingResponse> findAllByYearAndWeek(@Param("requestYear") int year, @Param("requestWeek") int week);

	Optional<CommunityRankingHistory> findByCommunityIdAndYearAndWeek(
		@Param("communityId") Long communityId,
		@Param("requestYear") int year,
		@Param("requestWeek") int week);
}