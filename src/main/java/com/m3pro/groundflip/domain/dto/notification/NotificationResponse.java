package com.m3pro.groundflip.domain.dto.notification;

import java.time.LocalDateTime;

import com.m3pro.groundflip.domain.entity.Notification;
import com.m3pro.groundflip.domain.entity.UserNotification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "알림 리스트의 정보")
public class NotificationResponse {
	@Schema(description = "알림 id", example = "1")
	private Long notificationId;

	@Schema(description = "알림 카테고리", example = "1")
	private String category;

	@Schema(description = "알림 id", example = "1")
	private Long categoryId;

	@Schema(description = "알림 제목", example = "1")
	private String title;

	@Schema(description = "알림 날짜", example = "1")
	private LocalDateTime date;

	@Schema(description = "알림 내용에 해당하는 데이터 id", example = "1")
	private Long contentId;

	@Schema(description = "알림 내용", example = "1")
	private String contents;

	@Schema(description = "알림 읽었는지 여부", example = "1")
	private boolean isRead;

	public static NotificationResponse from(UserNotification userNotification) {
		Notification notification = userNotification.getNotification();
		return NotificationResponse.builder()
			.notificationId(userNotification.getId())
			.category(notification.getCategory())
			.categoryId(notification.getCategoryId())
			.title(notification.getTitle())
			.date(userNotification.getCreatedAt())
			.contentId(notification.getContentId())
			.contents(notification.getContent())
			.isRead(userNotification.getReadAt() != null)
			.build();
	}
}
