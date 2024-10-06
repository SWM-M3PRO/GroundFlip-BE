package com.m3pro.groundflip.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.entity.CompetitionCount;
import com.m3pro.groundflip.domain.entity.Region;

public interface CompetitionCountRepository extends JpaRepository<CompetitionCount, Long> {
	@Query("SELECT c FROM CompetitionCount c WHERE c.region = :id AND c.week = :week AND c.year = :year")
	Optional<CompetitionCount> findByRegion(
		@Param("id") Region region,
		@Param("week") int week,
		@Param("year") int year
	);
}
