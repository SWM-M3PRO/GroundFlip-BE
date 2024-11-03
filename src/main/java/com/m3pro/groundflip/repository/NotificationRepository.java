package com.m3pro.groundflip.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.m3pro.groundflip.domain.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
