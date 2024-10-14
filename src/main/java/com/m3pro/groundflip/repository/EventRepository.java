package com.m3pro.groundflip.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.m3pro.groundflip.domain.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
