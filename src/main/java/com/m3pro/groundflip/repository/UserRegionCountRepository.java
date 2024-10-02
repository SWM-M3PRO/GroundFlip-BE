package com.m3pro.groundflip.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.entity.Region;
import com.m3pro.groundflip.domain.entity.UserRegionCount;

public interface UserRegionCountRepository extends JpaRepository<UserRegionCount, Long> {
	@Query("SELECT u FROM UserRegionCount u WHERE u.region = :region AND u.user.id = :user_id")
	Optional<UserRegionCount> findByRegionAndUser(
		@Param("region") Region region,
		@Param("user_id") Long userId
	);
}
