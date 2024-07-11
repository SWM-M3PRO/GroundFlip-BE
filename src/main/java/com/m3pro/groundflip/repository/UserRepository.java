package com.m3pro.groundflip.repository;

import com.m3pro.groundflip.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import com.m3pro.groundflip.domain.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndEmail(Provider provider, String email);
}
