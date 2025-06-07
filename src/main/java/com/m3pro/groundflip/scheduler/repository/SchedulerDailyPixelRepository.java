package com.m3pro.groundflip.scheduler.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.m3pro.groundflip.domain.entity.DailyPixel;
import com.m3pro.groundflip.scheduler.dto.DailyPixelResponse;

public interface SchedulerDailyPixelRepository extends JpaRepository<DailyPixel, Integer> {

	@Query(value = """
		SELECT user_id, COUNT(distinct pixel_id) AS daily_pixel_count
				FROM pixel_user
				WHERE created_at < :today and created_at >= DATE_SUB(:today, INTERVAL 1 DAY)
				GROUP BY user_id;
		""", nativeQuery = true)
	List<DailyPixelResponse> findDailyPixelByDate(LocalDate today);
}
