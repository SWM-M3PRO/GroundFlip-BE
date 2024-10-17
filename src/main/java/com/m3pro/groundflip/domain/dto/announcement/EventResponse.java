package com.m3pro.groundflip.domain.dto.announcement;

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
public class EventResponse {
	@Schema(description = "이벤트 사진", example = "www.example.com")
	private String eventImageUrl;

	@Schema(description = "이벤트와 관련된 공지사항 id", example = "3")
	private Long announcementId;

	@Schema(description = "이벤트 id", example = "1")
	private Long eventId;
}
