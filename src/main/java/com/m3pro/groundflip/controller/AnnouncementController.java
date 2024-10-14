package com.m3pro.groundflip.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.announcement.AnnouncementResponse;
import com.m3pro.groundflip.domain.dto.announcement.EventResponse;
import com.m3pro.groundflip.service.AnnouncementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/announcement")
@Tag(name = "announcement", description = "공지 API")
@SecurityRequirement(name = "Authorization")
public class AnnouncementController {
	private final AnnouncementService announcementService;

	@Operation(summary = "이벤트 목록을 조회한다.", description = "현재 진행중인 이벤트 목록을 조회한다. 이벤트 사진, 공지사항 id 를 반환한다.")
	@GetMapping("/events")
	public Response<List<EventResponse>> getUserInfo() {
		return Response.createSuccess(announcementService.getEvents());
	}

	@Operation(summary = "공지 목록을 조회한다.", description = "공지 목록을 조회한다. 커서 이후의 공지사항 30개를 불러온다.")
	@GetMapping("")
	public Response<List<AnnouncementResponse>> getAnnouncements(
		@RequestParam(name = "cursor", defaultValue = "0") Long cursor
	) {
		return Response.createSuccess(announcementService.getAnnouncements(cursor));
	}
}
