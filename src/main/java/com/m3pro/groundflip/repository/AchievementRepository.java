package com.m3pro.groundflip.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.dto.achievement.AchievementElementInterface;
import com.m3pro.groundflip.domain.entity.Achievement;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
	@Query(value = """
		SELECT a.achievement_id AS achievementId,
			a.name AS achievementName,
			a.badge_image_url AS badgeImageUrl,
			ua.obtained_at AS obtainedDate,
			a.category_id AS categoryId
		FROM achievement a
		LEFT JOIN user_achievement ua
			ON a.achievement_id = ua.achievement_id
			AND ua.user_id = :user_id
			AND ua.obtained_at is not null
		WHERE category_id = :achievement_category_id
		""", nativeQuery = true)
	List<AchievementElementInterface> findAllByCategory(
		@Param("achievement_category_id") Long achievementCategoryId,
		@Param("user_id") Long userId
	);

	@Query("SELECT a FROM Achievement a WHERE a.categoryId = :categoryId ORDER BY a.id ASC")
	Optional<Achievement> findByCategoryId(Long categoryId);
}
