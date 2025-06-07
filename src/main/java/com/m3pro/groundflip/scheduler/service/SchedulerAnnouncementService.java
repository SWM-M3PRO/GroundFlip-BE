package com.m3pro.groundflip.scheduler.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.entity.Announcement;
import com.m3pro.groundflip.scheduler.repository.SchedulerAnnouncementRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerAnnouncementService {
	private final SchedulerAnnouncementRepository schedulerAnnouncementRepository;

	@Transactional
	public Long createAnnouncement(String title, String content) {
		Announcement announcement = schedulerAnnouncementRepository.saveAndFlush(Announcement.builder()
			.title(title)
			.content(content)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.viewCount(0L)
			.build());
		return announcement.getId();
	}
}
