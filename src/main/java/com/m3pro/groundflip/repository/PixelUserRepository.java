package com.m3pro.groundflip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.entity.PixelUser;

public interface PixelUserRepository extends JpaRepository<PixelUser, Long> {
	@Query(value = """
		select pu.pixel_id, pu.user_id, u.nickname, u.profile_image from pixel_user pu
		        join user u
		          on pu.user_id = u.user_id
		         where pu.pixel_id = :pixel_id
		        group by pu.user_id;
		""", nativeQuery = true)
	List<Object[]> findAllVisitedUserByPixelId(
		@Param("pixel_id") int pixelId);

	@Query(value = """
		select pu.user_id, u.nickname, u.profile_image from pixel_user pu
		        join user u
		            on pu.user_id = u.user_id
		        where pu.pixel_id = :pixel_id
		        order by pu.created_at desc
		        limit 1;
		""", nativeQuery = true)
	List<Object[]> findCurrentOwnerByPixelId(
		@Param("pixel_id") int pixelId);
}
