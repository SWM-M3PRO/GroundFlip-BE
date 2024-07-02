package com.m3pro.groundflip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.dto.pixelUser.PixelOwnerUser;
import com.m3pro.groundflip.domain.dto.pixelUser.VisitedUser;
import com.m3pro.groundflip.domain.entity.PixelUser;

public interface PixelUserRepository extends JpaRepository<PixelUser, Long> {
	@Query(value = """
		select pu.pixel_id as pixelId, pu.user_id as userId, u.nickname as nickname, u.profile_image as profileImage from pixel_user pu
		        join user u
		          on pu.user_id = u.user_id
		         where pu.pixel_id = :pixel_id
		        group by pu.user_id;
		""", nativeQuery = true)
	List<VisitedUser> findAllVisitedUserByPixelId(
		@Param("pixel_id") int pixelId);

	@Query(value = """
		select pu.user_id as userId, u.nickname as nickname, u.profile_image as profileImage from pixel_user pu
		        join user u
		            on pu.user_id = u.user_id
		        where pu.pixel_id = :pixel_id
		        order by pu.created_at desc
		        limit 1;
		""", nativeQuery = true)
	PixelOwnerUser findCurrentOwnerByPixelId(
		@Param("pixel_id") int pixelId);

	@Query(value = """
		SELECT COUNT(DISTINCT pu.pixel_id) as unique_pixel_count
		FROM pixel_user pu
		WHERE pu.user_id = :user_id;
		""", nativeQuery = true)
	List<Object[]> findAccumulatePixelCountByUserId(
		@Param("user_id") int userId);

	@Query(value = """
		SELECT count(user_id)
		FROM (SELECT pu.*
		FROM pixel_user pu
		         JOIN (
		    SELECT pixel_id, MAX(created_at) AS latest_created_at
		    FROM pixel_user
		    WHERE pixel_id IN (SELECT DISTINCT pu.pixel_id as unique_pixel_count 
		                       FROM pixel_user pu WHERE pu.user_id = :user_id)
		    GROUP BY pixel_id
		) latest ON pu.pixel_id = latest.pixel_id AND pu.created_at = latest.latest_created_at
		WHERE pu.pixel_id IN (SELECT DISTINCT pu.pixel_id as unique_pixel_count 
		                      FROM pixel_user pu WHERE pu.user_id = :user_id)) res
		where res.user_id = :user_id
		""", nativeQuery = true)
	List<Object[]> findCurrentPixelCountByUserId(
		@Param("user_id") int userId);
}
