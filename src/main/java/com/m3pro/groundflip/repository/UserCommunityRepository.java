package com.m3pro.groundflip.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.entity.Community;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.domain.entity.UserCommunity;

public interface UserCommunityRepository extends JpaRepository<UserCommunity, Long> {
	@Query("SELECT uc FROM UserCommunity uc WHERE uc.user.id = :userId AND uc.deletedAt IS NULL")
	List<UserCommunity> findByUserId(@Param("userId") Long userId);

	@Query("SELECT COUNT(uc) FROM UserCommunity uc WHERE uc.community.id = :communityId AND uc.deletedAt IS NULL")
	Long countByCommunityId(@Param("communityId") Long communityId);

	Boolean existsByUserAndCommunityAndDeletedAtIsNull(User user, Community community);

	Optional<UserCommunity> findByUserAndCommunityAndDeletedAtIsNull(User user, Community community);

}
