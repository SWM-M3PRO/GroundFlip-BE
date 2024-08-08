package com.m3pro.groundflip.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.m3pro.groundflip.domain.entity.FcmToken;
import com.m3pro.groundflip.domain.entity.User;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
	Optional<FcmToken> findByUser(User user);
}
