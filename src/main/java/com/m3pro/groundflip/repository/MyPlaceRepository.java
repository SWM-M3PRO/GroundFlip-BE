package com.m3pro.groundflip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m3pro.groundflip.domain.entity.MyPlace;
import com.m3pro.groundflip.enums.Place;

public interface MyPlaceRepository extends JpaRepository<MyPlace, Long> {

	@Query(value = """
		SELECT mp.*
		FROM my_place mp
		JOIN (
		    SELECT place_name, MAX(created_at) AS latest_created_at
		    FROM my_place
		    WHERE user_id = :userId
		    GROUP BY place_name
		) grouped_mp
		ON mp.place_name = grouped_mp.place_name AND mp.created_at = grouped_mp.latest_created_at
		WHERE mp.user_id = :userId
		""", nativeQuery = true)
	List<MyPlace> findByUserId(@Param("userId") Long userId);

	List<MyPlace> findByUserIdAndPlaceName(Long userId, Place placeName);
}
