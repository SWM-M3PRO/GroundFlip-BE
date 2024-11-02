package com.m3pro.groundflip.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.entity.UserNotification;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
	@Query("""
		SELECT un FROM UserNotification un
		JOIN FETCH un.notification
		WHERE un.userId = :userId
		AND un.createdAt > :lookup_date
		""")
	List<UserNotification> findAllByUserId(@Param("user_id") Long userId,
		@Param("lookup_date") LocalDateTime lookupDate);
}
