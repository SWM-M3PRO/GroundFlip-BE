package com.m3pro.groundflip.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Event {
	@Id
	@Column(name = "event_id")
	private Long id;

	private String eventImage;

	private Long announcementId;

	private LocalDateTime startDate;

	private LocalDateTime endDate;
}
