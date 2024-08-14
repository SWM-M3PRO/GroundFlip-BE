package com.m3pro.groundflip.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.m3pro.groundflip.domain.entity.Preference;
import com.m3pro.groundflip.domain.entity.User;

public interface PreferenceRepository extends JpaRepository<Preference, Long> {
	Optional<Preference> findByUser(User user);
}
