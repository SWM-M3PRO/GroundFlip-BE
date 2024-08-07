package com.m3pro.groundflip.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.m3pro.groundflip.domain.entity.StepRecord;

public interface StepRecordRepository extends JpaRepository<StepRecord, Long> {
	List<StepRecord> findByUserIdAndDateBetween(Long userId, Date startDate, Date endDate);
}
