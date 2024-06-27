package com.m3pro.groundflip.repository;

import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.entity.Pixel;

public interface PixelRepository extends JpaRepository<Pixel, Long> {
	@SuppressWarnings("checkstyle:RegexpSinglelineJava")
	@Query(value = """
		WITH PixelsInRange AS (
		    SELECT
		        p.pixel_id,
		        p.coordinate,
		        p.x,
		        p.y
		    FROM
		        pixel p
		    WHERE
		        ST_CONTAINS((ST_Buffer(:center, :radius)), p.coordinate)
		),
		RecentVisits AS (
		    SELECT
		        pu.pixel_id,
		        pu.user_id,
		        pu.created_at,
		        ROW_NUMBER() OVER (PARTITION BY pu.pixel_id ORDER BY pu.created_at DESC) AS rn
		    FROM
		        pixel_user pu
		    JOIN
		        PixelsInRange pir ON pu.pixel_id = pir.pixel_id
		)
		SELECT
		    pir.pixel_id AS pixelId,
		    pir.coordinate,
		    rv.user_id AS userId,
		    pir.x,
		    pir.y
		FROM
		    PixelsInRange pir
		JOIN
		    RecentVisits rv ON pir.pixel_id = rv.pixel_id
		WHERE
		    rv.rn = 1
		""", nativeQuery = true)
	List<Object[]> findAllIndividualPixelsByCoordinate(
		@Param("center") Point center,
		@Param("radius") int radius);

	@Query(value = """
		WITH PixelsInRange AS (
		    SELECT
		        p.pixel_id,
		        p.coordinate,
		        p.x,
		        p.y
		    FROM
		        pixel p
		    WHERE
		        ST_CONTAINS((ST_Buffer(:center, :radius)), p.coordinate)
		)
		 SELECT
			distinct (pu.pixel_id),
			pir.coordinate,
			pir.x,
		    pir.y
		FROM
			pixel_user pu
		JOIN
			PixelsInRange pir ON pu.pixel_id = pir.pixel_id
		WHERE pu.user_id = :user_id
		""", nativeQuery = true)
	List<Object[]> findAllIndividualPixelsHistoryByCoordinate(
		@Param("center") Point center,
		@Param("radius") int radius,
		@Param("user_id") Long userId);
}
