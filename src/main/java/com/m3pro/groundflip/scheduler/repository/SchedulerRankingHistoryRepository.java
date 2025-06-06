package com.m3pro.groundflip.scheduler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.m3pro.groundflip.domain.entity.RankingHistory;

public interface SchedulerRankingHistoryRepository extends JpaRepository<RankingHistory, Long> {
	List<RankingHistory> findAllByYearAndWeek(Integer year, Integer week);

	@Query("""
			SELECT rh FROM RankingHistory rh
				WHERE rh.year = :year AND rh.week = :week
				ORDER BY rh.ranking
				LIMIT 100
		""")
	List<RankingHistory> findTop100AllByYearAndWeek(Integer year, Integer week);
}
