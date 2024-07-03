package com.m3pro.groundflip.repository;

import java.util.List;

import com.m3pro.groundflip.domain.entity.Pixel;
import com.m3pro.groundflip.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.dto.pixelUser.PixelCount;
import com.m3pro.groundflip.domain.dto.pixelUser.PixelOwnerUser;
import com.m3pro.groundflip.domain.dto.pixelUser.VisitedUser;
import com.m3pro.groundflip.domain.entity.PixelUser;

public interface PixelUserRepository extends JpaRepository<PixelUser, Long> {
	@Query(value = """
			SELECT pu.pixel_id AS pixelId,
			       pu.user_id AS userId,
			       u.nickname AS nickname,
			       u.profile_image AS profileImage 
			FROM pixel_user pu
			JOIN user u ON pu.user_id = u.user_id 
			WHERE pu.pixel_id = :pixel_id AND pu.created_at >= current_date() 
			GROUP BY pu.user_id;
			""", nativeQuery = true)
	List<VisitedUser> findAllVisitedUserByPixelId(
		@Param("pixel_id") Long pixelId);

	@Query(value = """
		SELECT pu.user_id AS userId,
		       u.nickname AS nickname,
		       u.profile_image AS profileImage 
		FROM pixel_user pu 
		JOIN user u ON pu.user_id = u.user_id 
		WHERE pu.pixel_id = :pixel_id 
		ORDER BY pu.created_at DESC 
		LIMIT 1
		""", nativeQuery = true)
	PixelOwnerUser findCurrentOwnerByPixelId(
		@Param("pixel_id") Long pixelId);

	@Query(value = """
		SELECT COUNT(DISTINCT pu.pixel_id) AS count 
		FROM pixel_user pu 
		WHERE pu.user_id = :user_id
		""", nativeQuery = true)
	PixelCount findAccumulatePixelCountByUserId(
		@Param("user_id") Long userId);

	@Query(value = """
		SELECT count(user_id) AS count 
		FROM (SELECT pu.*
		FROM pixel_user pu
		         JOIN (
		    SELECT pixel_id, MAX(created_at) AS latest_created_at
		    FROM pixel_user
		    WHERE pixel_id IN (SELECT DISTINCT pu.pixel_id AS unique_pixel_count 
		                       FROM pixel_user pu WHERE pu.user_id = :user_id)
		    GROUP BY pixel_id
		) latest ON pu.pixel_id = latest.pixel_id AND pu.created_at = latest.latest_created_at 
		WHERE pu.pixel_id IN (SELECT DISTINCT pu.pixel_id AS unique_pixel_count  
		                      FROM pixel_user pu WHERE pu.user_id = :user_id)) res
		where res.user_id = :user_id 
		""", nativeQuery = true)
	PixelCount findCurrentPixelCountByUserId(
		@Param("user_id") Long userId);

	List<PixelUser> findAllByPixelAndUserOrderByCreatedAt(Pixel pixel, User user);
}
