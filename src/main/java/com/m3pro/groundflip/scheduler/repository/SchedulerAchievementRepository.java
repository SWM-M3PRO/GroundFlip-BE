package com.m3pro.groundflip.scheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.m3pro.groundflip.domain.entity.Achievement;

public interface SchedulerAchievementRepository extends JpaRepository<Achievement, Long> {
}
