package com.m3pro.groundflip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.entity.Announcement;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
	@Query("SELECT a FROM Announcement a WHERE a.id > :cursor ORDER BY a.id ASC LIMIT :size")
	List<Announcement> findAllAnnouncement(@Param("cursor") Long cursor, @Param("size") int size);
}
