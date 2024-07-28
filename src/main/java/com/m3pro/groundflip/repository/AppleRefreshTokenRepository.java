package com.m3pro.groundflip.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.m3pro.groundflip.domain.entity.AppleRefreshToken;

public interface AppleRefreshTokenRepository extends JpaRepository<AppleRefreshToken, Long> {
	Optional<AppleRefreshToken> findByUserId(Long userId);
}
