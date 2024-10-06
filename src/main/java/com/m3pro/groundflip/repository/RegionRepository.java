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
				r.name,
				cc.individual_mode_count AS count
			FROM
				region r
			JOIN competition_count cc
			ON r.region_id = cc.region_id
			WHERE
				ST_CONTAINS((ST_Buffer(:center, :radius)), r.coordinate)
				AND r.region_level = 'city'
				AND cc.week = :week
				AND cc.year = :year
				AND cc.individual_mode_count > 0
		""", nativeQuery = true)
	List<RegionInfo> findAllIndividualCityRegionsByCoordinate(
		@Param("center") Point center,
		@Param("radius") int radius,
		@Param("week") int week,
		@Param("year") int year
	);

	@Query(value = """
			SELECT
				p.region_id,
				ST_LATITUDE(p.coordinate) AS latitude,
				ST_LONGITUDE(p.coordinate) AS longitude,
				p.name,
				SUM(cc.individual_mode_count) AS count
			FROM
				region p
			JOIN region r
			ON p.region_id = r.parent_id
			JOIN competition_count cc
			ON r.region_id = cc.region_id
			WHERE
				p.region_level = 'province'
				AND cc.week = :week
				AND cc.year = :year
				AND cc.individual_mode_count > 0
			GROUP BY
				p.region_id
		""", nativeQuery = true)
	List<RegionInfo> findAllIndividualProvinceRegionsByCoordinate(
		@Param("week") int week,
		@Param("year") int year
	);

	@Query(value = """
			SELECT
				r.region_id,
				ST_LATITUDE(r.coordinate) AS latitude,
				ST_LONGITUDE(r.coordinate) AS longitude,
				r.name,
				cc.community_mode_count AS count
			FROM
				region r
			JOIN competition_count cc
			ON r.region_id = cc.region_id
			WHERE
				ST_CONTAINS((ST_Buffer(:center, :radius)), r.coordinate)
				AND r.region_level = 'city'
				AND cc.week = :week
				AND cc.year = :year
				AND cc.community_mode_count > 0
		""", nativeQuery = true)
	List<RegionInfo> findAllCommunityCityRegionsByCoordinate(
		@Param("center") Point center,
		@Param("radius") int radius,
		@Param("week") int week,
		@Param("year") int year
	);

	@Query(value = """
			SELECT
				p.region_id,
				ST_LATITUDE(p.coordinate) AS latitude,
				ST_LONGITUDE(p.coordinate) AS longitude,
				p.name,
				SUM(cc.community_mode_count) AS count
			FROM
				region p
			JOIN region r
			ON p.region_id = r.parent_id
			JOIN competition_count cc
			ON r.region_id = cc.region_id
			WHERE
				p.region_level = 'province'
				AND cc.week = :week
				AND cc.year = :year
				AND cc.community_mode_count > 0
			GROUP BY
				p.region_id
		""", nativeQuery = true)
	List<RegionInfo> findAllCommunityProvinceRegionsByCoordinate(
		@Param("week") int week,
		@Param("year") int year
	);

	@Query(value = """
			SELECT
				r.region_id,
				ST_LATITUDE(r.coordinate) AS latitude,
				ST_LONGITUDE(r.coordinate) AS longitude,
				r.name,
				urc.count AS count
			FROM
				region r
			JOIN user_region_count urc
			ON r.region_id = urc.region_id
			WHERE
				ST_CONTAINS((ST_Buffer(:center, :radius)), r.coordinate)
				AND r.region_level = 'city'
				AND urc.count > 0
				AND urc.user_id = :user_id
		""", nativeQuery = true)
	List<RegionInfo> findAllIndividualHistoryCityRegionsByCoordinate(
		@Param("center") Point center,
		@Param("radius") int radius,
		@Param("user_id") Long userId
	);

	@Query(value = """
			SELECT
				p.region_id,
				ST_LATITUDE(p.coordinate) AS latitude,
				ST_LONGITUDE(p.coordinate) AS longitude,
				p.name,
				SUM(urc.count) AS count
			FROM
				region p
			JOIN region r
			ON p.region_id = r.parent_id
			JOIN user_region_count urc
			ON r.region_id = urc.region_id
			WHERE
				p.region_level = 'province'
				AND urc.count > 0
				AND urc.user_id = :user_id
			GROUP BY
				p.region_id
		""", nativeQuery = true)
	List<RegionInfo> findAllIndividualHistoryProvinceRegionsByCoordinate(
		@Param("user_id") Long userId
	);
}
