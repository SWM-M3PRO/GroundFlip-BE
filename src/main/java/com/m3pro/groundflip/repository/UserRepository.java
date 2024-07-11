package com.m3pro.groundflip.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.enums.Provider;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByProviderAndEmail(Provider provider, String email);
}
