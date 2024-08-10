package com.m3pro.groundflip.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.m3pro.groundflip.domain.entity.AppVersion;

public interface AppVersionRepository extends CrudRepository<AppVersion, Long> {

	@Query(value = """
		SELECT av.*
		FROM app_version av
		ORDER BY av.created_date DESC 
		limit 1
		""", nativeQuery = true)
	Optional<AppVersion> findLaestetVersion();
}
