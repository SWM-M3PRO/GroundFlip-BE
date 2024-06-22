package com.m3pro.groundflip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.entity.Pixel;

public interface PixelRepository extends JpaRepository<Pixel, Long> {
	@Query(value = "select pixel from Pixel pixel "
		+ "where pixel.x between :currentX - :xRange / 2 and :currentX + :xRange / 2 "
		+ "and pixel.y between :currentY - :yRange / 2 and :currentY + :yRange / 2 ")
	List<Pixel> findAllNearPixels(@Param("currentX") int currentX, @Param("currentY") int currentY,
		@Param("xRange") int xRange, @Param("yRange") int yRange);
}
