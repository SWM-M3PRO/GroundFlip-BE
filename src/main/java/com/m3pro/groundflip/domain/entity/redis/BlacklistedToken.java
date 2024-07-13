package com.m3pro.groundflip.domain.entity.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@RedisHash("token")
@AllArgsConstructor
@NoArgsConstructor
public class BlacklistedToken {
	@Id
	private String token;

	private String status;

	@TimeToLive
	private Long timeToLive;

	public BlacklistedToken(String token, Long timeToLive) {
		this.token = token;
		this.status = "logout";
		this.timeToLive = timeToLive;
	}
}
