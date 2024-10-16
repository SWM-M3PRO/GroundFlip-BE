package com.m3pro.groundflip.domain.dto.announcement;

import java.time.LocalDateTime;

import com.m3pro.groundflip.domain.entity.Announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "이벤트 정보")
public class AnnouncementInfoResponse {
	@Schema(description = "공지 제목", example = "제목")
	private String title;

	@Schema(description = "공지 id", example = "1")
	private Long announcementId;

	@Schema(description = "공지 작성일", example = "2024-10-18")
	private LocalDateTime createdAt;

	@Schema(description = "공지 내용", example = "안녕하세요")
	private String content;

	public static AnnouncementInfoResponse from(Announcement announcement) {
		return AnnouncementInfoResponse.builder()
			.title(announcement.getTitle())
			.createdAt(announcement.getCreatedAt())
			.content(announcement.getContent())
			.announcementId(announcement.getId())
			.build();
	}
}
