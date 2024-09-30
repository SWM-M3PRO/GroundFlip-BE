package com.m3pro.groundflip.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.m3pro.groundflip.domain.entity.Pixel;

public interface RegionRepository extends JpaRepository<Pixel, Long> {
}
