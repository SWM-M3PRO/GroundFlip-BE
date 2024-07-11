package com.m3pro.groundflip.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.entity.StepRecord;

public interface StepRecordRepository extends JpaRepository<StepRecord, Long> {
	@Query("SELECT sr.steps FROM StepRecord sr "
		+ "WHERE sr.user.id = :userId AND sr.date BETWEEN :startDate AND :endDate")
	List<Integer> findByUserIdStartEndDate(
		Long userId,
		Date startDate,
		Date endDate
	);
}
