package com.m3pro.groundflip.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
	@Query("SELECT e FROM Event e WHERE e.startDate <= :today AND e.endDate >= :today")
	List<Event> findCurrentEvents(@Param("today") LocalDateTime today);
}
