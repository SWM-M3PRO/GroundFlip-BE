package com.m3pro.groundflip.scheduler.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.entity.FcmToken;
import com.m3pro.groundflip.domain.entity.User;

public interface SchedulerFcmTokenRepository extends JpaRepository<FcmToken, Long> {
	Optional<FcmToken> findByUser(User user);

	@Query("""
		SELECT f FROM FcmToken f
		JOIN Permission p ON f.user.id = p.user.id
		WHERE p.serviceNotificationsEnabled = true
		""")
	List<FcmToken> findAllTokensForServiceNotifications();

	@Query("""
		SELECT f FROM FcmToken f
		JOIN Permission p ON f.user.id = p.user.id
		WHERE f.device = 'IOS' AND p.serviceNotificationsEnabled = true
		""")
	List<FcmToken> findAllIOSTokensForServiceNotification();

	@Query("""
		SELECT f FROM FcmToken f
		JOIN Permission p ON f.user.id = p.user.id
		WHERE f.device = 'ANDROID' AND p.serviceNotificationsEnabled = true
		""")
	List<FcmToken> findAllAndroidTokensForServiceNotification();

	@Query("""
		SELECT f FROM FcmToken f
		JOIN Permission p ON f.user.id = p.user.id
		WHERE p.marketingNotificationsEnabled = true
		""")
	List<FcmToken> findAllTokensForMarketingNotifications();

	@Query("""
		SELECT f FROM FcmToken f
		JOIN Permission p ON f.user.id = p.user.id
		WHERE f.device = 'IOS' AND p.marketingNotificationsEnabled = true
		""")
	List<FcmToken> findAllIOSTokensForMarketingNotification();

	@Query("""
		SELECT f FROM FcmToken f
		JOIN Permission p ON f.user.id = p.user.id
		WHERE f.device = 'ANDROID' AND p.marketingNotificationsEnabled = true
		""")
	List<FcmToken> findAllAndroidTokensForMarketingNotification();

	@Query("""
		SELECT f FROM FcmToken f
		WHERE f.device = 'ANDROID'
		""")
	List<FcmToken> findAllAndroidTokensForStepNotification();

	@Query("""
		SELECT f FROM FcmToken f
		JOIN Permission p ON f.user.id = p.user.id
		WHERE p.serviceNotificationsEnabled = true
		AND f.user.id = :user_id
		""")
	Optional<FcmToken> findTokenForServiceNotifications(@Param("user_id") Long userId);

	void deleteByUser(User user);

	void deleteByTokenIn(Collection<String> token);
}
