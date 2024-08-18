package com.m3pro.groundflip.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.m3pro.groundflip.domain.entity.Permission;
import com.m3pro.groundflip.domain.entity.User;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
	Optional<Permission> findByUser(User user);
}
