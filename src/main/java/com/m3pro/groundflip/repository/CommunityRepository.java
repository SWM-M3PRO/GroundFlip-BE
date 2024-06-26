package com.m3pro.groundflip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.entity.Community;

public interface CommunityRepository extends JpaRepository<Community, Long> {

	List<Community> findByNameLike(String name);
}
