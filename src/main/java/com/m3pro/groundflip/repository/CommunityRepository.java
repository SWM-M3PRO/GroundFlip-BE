package com.m3pro.groundflip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.m3pro.groundflip.domain.entity.Community;

public interface CommunityRepository extends JpaRepository<Community, Long> {

	List<Community> findAllByNameLike(String name);
}
