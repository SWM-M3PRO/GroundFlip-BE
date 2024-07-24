package com.m3pro.groundflip.repository;

import java.util.List;

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
			WHERE rh.year = :requestYear AND rh.week = :requestWeek
			ORDER BY rh.ranking asc
			LIMIT 30 
		""")
	List<UserRankingResponse> findAllByYearAndWeek(@Param("requestYear") int year, @Param("requestWeek") int week);
}
