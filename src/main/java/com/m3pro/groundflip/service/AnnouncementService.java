package com.m3pro.groundflip.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.announcement.EventResponse;
import com.m3pro.groundflip.domain.entity.Event;
import com.m3pro.groundflip.repository.EventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementService {
	private final EventRepository eventRepository;

	public List<EventResponse> getEvents() {
		LocalDateTime today = LocalDateTime.now();
		List<Event> events = eventRepository.findCurrentEvents(today);

		return events.stream().map((event -> EventResponse.builder()
			.eventImageUrl(event.getEventImage())
			.announcementId(event.getAnnouncementId())
			.build())).toList();
	}
}
