package com.m3pro.groundflip.domain.entity;

import com.m3pro.groundflip.domain.entity.global.BaseTimeEntity;
import com.m3pro.groundflip.enums.Device;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "fcm_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FcmToken extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "fcm_token_id")
	private Long id;

	private String token;

	@Enumerated(EnumType.STRING)
	private Device device;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	public void updateToken(String token) {
		this.token = token;
	}

	public void updateDevice(Device device) {
		this.device = device;
	}
}
