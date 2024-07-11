package com.m3pro.groundflip.repository;

import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.dto.pixel.IndividualHistoryPixelResponse;
import com.m3pro.groundflip.domain.dto.pixel.IndividualModePixelResponse;
import com.m3pro.groundflip.domain.entity.Pixel;

public interface PixelRepository extends JpaRepository<Pixel, Long> {
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
		    ST_LATITUDE(pir.coordinate) AS latitude,
		    ST_LONGITUDE(pir.coordinate) AS longitude,
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
	List<IndividualModePixelResponse> findAllIndividualPixelsByCoordinate(
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
			DISTINCT (pu.pixel_id) AS pixelId,
			ST_LATITUDE(pir.coordinate) AS latitude,
			ST_LONGITUDE(pir.coordinate) AS longitude,
			pir.x,
		    pir.y
		FROM
			pixel_user pu
		JOIN
			PixelsInRange pir ON pu.pixel_id = pir.pixel_id
		WHERE pu.user_id = :user_id
		""", nativeQuery = true)
	List<IndividualHistoryPixelResponse> findAllIndividualPixelsHistoryByCoordinate(
		@Param("center") Point center,
		@Param("radius") int radius,
		@Param("user_id") Long userId);

	Optional<Pixel> findByXAndY(Long x, Long y);
}
