package com.m3pro.groundflip.domain.entity.redis;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@RedisHash("token")
@AllArgsConstructor
@NoArgsConstructor
public class BlacklistedToken {
	@Id
	private String id;

	private String token;

	@TimeToLive
	private Long timeToLive;

	public BlacklistedToken(String token, Long timeToLive) {
		this.id = token;
		this.token = "logout";
		this.timeToLive = timeToLive;
	}
}
