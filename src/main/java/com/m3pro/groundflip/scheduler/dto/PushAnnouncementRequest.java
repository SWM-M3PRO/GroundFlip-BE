package com.m3pro.groundflip.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PushAnnouncementRequest {
	private String title;

	private String content;

	private String message;

	private String secretKey;
}
