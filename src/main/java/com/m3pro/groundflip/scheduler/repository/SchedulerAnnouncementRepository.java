package com.m3pro.groundflip.scheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.m3pro.groundflip.domain.entity.Announcement;

public interface SchedulerAnnouncementRepository extends JpaRepository<Announcement, Long> {
}
