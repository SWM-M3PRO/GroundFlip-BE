package com.m3pro.groundflip.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.announcement.AnnouncementInfoResponse;
import com.m3pro.groundflip.domain.dto.announcement.AnnouncementResponse;
import com.m3pro.groundflip.domain.dto.announcement.EventResponse;
import com.m3pro.groundflip.domain.entity.Announcement;
import com.m3pro.groundflip.domain.entity.Event;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.AnnouncementRepository;
import com.m3pro.groundflip.repository.EventRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementService {
	private static final int PAGE_SIZE = 30;
	private final EventRepository eventRepository;
	private final AnnouncementRepository announcementRepository;

	public List<EventResponse> getEvents() {
		LocalDateTime today = LocalDateTime.now();
		List<Event> events = eventRepository.findCurrentEvents(today);

		return events.stream().map((event -> EventResponse.builder()
			.eventImageUrl(event.getEventImage())
			.announcementId(event.getAnnouncementId())
			.build())).toList();
	}

	public List<AnnouncementResponse> getAnnouncements(Long cursor) {
		List<Announcement> announcements = announcementRepository.findAllAnnouncement(cursor, PAGE_SIZE);

		return announcements.stream()
			.map(announcement -> AnnouncementResponse.builder()
				.announcementId(announcement.getId())
				.title(announcement.getTitle())
				.createdAt(announcement.getCreatedAt())
				.build()
			).toList();
	}

	@Transactional
	public AnnouncementInfoResponse getAnnouncementInfo(Long announcementId) {
		Announcement announcement = announcementRepository.findById(announcementId)
			.orElseThrow(() -> new AppException(
				ErrorCode.INTERNAL_SERVER_ERROR));
		announcement.incrementViewCount();
		return AnnouncementInfoResponse.from(announcement);
	}
}
