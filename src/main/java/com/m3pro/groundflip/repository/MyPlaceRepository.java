package com.m3pro.groundflip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.m3pro.groundflip.domain.entity.MyPlace;
import com.m3pro.groundflip.enums.Place;

public interface MyPlaceRepository extends JpaRepository<MyPlace, Long> {

	@Query(value = """
		SELECT mp.*
		    FROM my_place mp
		    JOIN (
		        SELECT place_name, MAX(my_place_id) AS max_place_mark_id
		        FROM my_place
		        GROUP BY place_name
		    ) grouped_mp
		    ON mp.my_place_id = grouped_mp.max_place_mark_id
		    WHERE mp.user_id = :userId
		""", nativeQuery = true)
	List<MyPlace> findByUserId(Long userId);

	List<MyPlace> findByUserIdAndPlaceName(Long userId, Place placeName);
}
