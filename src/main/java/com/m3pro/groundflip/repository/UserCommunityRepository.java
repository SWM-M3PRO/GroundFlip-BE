package com.m3pro.groundflip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.entity.UserCommunity;

public interface UserCommunityRepository extends JpaRepository<UserCommunity, Long> {
	@Query("SELECT uc FROM UserCommunity uc WHERE uc.user.id = :userId AND uc.deletedAt IS NULL")
	List<UserCommunity> findByUserId(@Param("userId") Long userId);
}
