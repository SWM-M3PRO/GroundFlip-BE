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
	@Schema(description = "사용자 닉네임", example = "홍길동")
	private String eventImageUrl;

	@Schema(description = "사용자 출생년도", example = "2000")
	private Long announcementId;
}
