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
		SELECT
		    pixel.pixel_id AS pixelId,
		    ST_LATITUDE(pixel.coordinate) AS latitude,
		    ST_LONGITUDE(pixel.coordinate) AS longitude,
		    pixel.user_id AS userId,
		    pixel.x,
		    pixel.y
		FROM
		    pixel
		WHERE
		    ST_CONTAINS((ST_Buffer(:center, :radius)), pixel.coordinate) AND pixel.user_id IS NOT NULL
		""", nativeQuery = true)
	List<IndividualModePixelResponse> findAllIndividualModePixelsByCoordinate(
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
