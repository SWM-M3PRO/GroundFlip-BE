package com.m3pro.groundflip.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.m3pro.groundflip.domain.entity.redis.BlacklistedToken;

@Repository
public interface BlackListedTokenRepository extends CrudRepository<BlacklistedToken, String> {
}
