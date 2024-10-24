package com.m3pro.groundflip.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.announcement.AnnouncementInfoResponse;
import com.m3pro.groundflip.domain.dto.announcement.AnnouncementResponse;
import com.m3pro.groundflip.domain.dto.announcement.EventResponse;
import com.m3pro.groundflip.domain.entity.Announcement;
import com.m3pro.groundflip.domain.entity.Event;
import com.m3pro.groundflip.repository.AnnouncementRepository;
import com.m3pro.groundflip.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

	@Mock
	private EventRepository eventRepository;

	@Mock
	private AnnouncementRepository announcementRepository;

	@InjectMocks
	private AnnouncementService announcementService;

	@Test
	@DisplayName("[getEvents] 이벤트 목록을 정상적으로 반환한다.")
	void testGetEvents() {
		// Given
		LocalDateTime today = LocalDateTime.now();
		Event mockEvent = Event.builder()
			.id(1L)
			.announcementId(10L)
			.eventImage("https://example.com/image.jpg")
			.build();
		when(eventRepository.findCurrentEvents(any())).thenReturn(Collections.singletonList(mockEvent));

		// When
		List<EventResponse> result = announcementService.getEvents();

		// Then
		assertEquals(1, result.size());
		assertEquals(1L, result.get(0).getEventId());
		assertEquals(10L, result.get(0).getAnnouncementId());
		assertEquals("https://example.com/image.jpg", result.get(0).getEventImageUrl());
	}

	@Test
	@DisplayName("[getAnnouncements] 최근 공지사항 목록을 반환한다.")
	void testGetAnnouncements() {
		// Given
		Announcement mockAnnouncement = Announcement.builder()
			.id(1L)
			.title("New Announcement")
			.createdAt(LocalDateTime.now())
			.build();
		when(announcementRepository.findAllRecentAnnouncement(anyInt())).thenReturn(
			Collections.singletonList(mockAnnouncement));

		// When
		List<AnnouncementResponse> result = announcementService.getAnnouncements(0L);

		// Then
		assertEquals(1, result.size());
		assertEquals(1L, result.get(0).getAnnouncementId());
		assertEquals("New Announcement", result.get(0).getTitle());
	}

	@Test
	@DisplayName("[getAnnouncementInfo] 공지사항 정보를 정상적으로 반환하고 조회수를 증가시킨다.")
	void testGetAnnouncementInfo() {
		// Given
		Announcement mockAnnouncement = Announcement.builder()
			.id(1L)
			.title("Important Announcement")
			.viewCount(0L)
			.createdAt(LocalDateTime.now())
			.build();
		when(announcementRepository.findById(1L)).thenReturn(Optional.of(mockAnnouncement));

		// When
		AnnouncementInfoResponse result = announcementService.getAnnouncementInfo(1L);

		// Then
		assertEquals(1L, result.getAnnouncementId());
		assertEquals("Important Announcement", result.getTitle());
		assertEquals(1, mockAnnouncement.getViewCount()); // View count should be incremented
	}

	@Test
	@DisplayName("[increaseViewCount] 이벤트 조회수를 증가시킨다.")
	void testIncreaseViewCount() {
		// Given
		Event mockEvent = Event.builder()
			.id(1L)
			.viewCount(0L)
			.build();
		when(eventRepository.findById(1L)).thenReturn(Optional.of(mockEvent));

		// When
		announcementService.increaseViewCount(1L);

		// Then
		assertEquals(1, mockEvent.getViewCount()); // View count should be incremented
	}
}
