package com.m3pro.groundflip.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.entity.UserAchievement;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
	@Query("""
		SELECT COUNT(*) FROM UserAchievement ua
		WHERE ua.id = :user_id
		AND ua.obtainedAt is not null
		""")
	Integer countByUserId(@Param("user_id") Long userId);

	@Query("""
		SELECT ua FROM UserAchievement ua
			JOIN FETCH ua.achievement a
			WHERE ua.user.id = :userId AND ua.obtainedAt IS NOT NULL
			ORDER BY ua.obtainedAt DESC
		""")
	List<UserAchievement> findAllByUserId(Long userId, Pageable pageable);

	@Query("""
		SELECT ua FROM UserAchievement ua
			JOIN FETCH ua.achievement a
			WHERE ua.user.id = :userId AND ua.obtainedAt IS NOT NULL
			ORDER BY ua.obtainedAt DESC
		""")
	List<UserAchievement> findAllByUserId(Long userId);
}
