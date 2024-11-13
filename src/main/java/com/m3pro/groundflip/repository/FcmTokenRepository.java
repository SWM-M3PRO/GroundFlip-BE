package com.m3pro.groundflip.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.entity.FcmToken;
import com.m3pro.groundflip.domain.entity.User;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
	Optional<FcmToken> findByUser(User user);

	void deleteByUser(User user);

	@Query("""
		SELECT f FROM FcmToken f
		JOIN Permission p ON f.user.id = p.user.id
		WHERE p.serviceNotificationsEnabled = true
		AND f.user.id = :user_id
		""")
	Optional<FcmToken> findTokenForServiceNotifications(@Param("user_id") Long userId);
}
