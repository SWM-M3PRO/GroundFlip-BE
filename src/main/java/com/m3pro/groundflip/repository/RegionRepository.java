package com.m3pro.groundflip.repository;

import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.dto.pixel.RegionInfo;
import com.m3pro.groundflip.domain.entity.Region;

public interface RegionRepository extends JpaRepository<Region, Long> {
	@Query(value = """
			SELECT
				r.region_id,
				ST_LATITUDE(r.coordinate) AS latitude,
				ST_LONGITUDE(r.coordinate) AS longitude,
				r.name
			FROM
				region r
			WHERE
				ST_CONTAINS((ST_Buffer(:center, :radius)), r.coordinate)
				AND r.region_level = :region_level
		""", nativeQuery = true)
	List<RegionInfo> findAllCityRegionsByCoordinate(
		@Param("center") Point center,
		@Param("radius") int radius,
		@Param("region_level") String regionLevel
	);
}
