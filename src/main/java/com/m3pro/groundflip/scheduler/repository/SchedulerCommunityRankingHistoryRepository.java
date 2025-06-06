package com.m3pro.groundflip.scheduler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.m3pro.groundflip.domain.entity.CommunityRankingHistory;

public interface SchedulerCommunityRankingHistoryRepository extends JpaRepository<CommunityRankingHistory, Long> {
	List<CommunityRankingHistory> findAllByYearAndWeek(Integer year, Integer week);
}