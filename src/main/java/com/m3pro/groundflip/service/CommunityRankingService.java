package com.m3pro.groundflip.service;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.repository.CommunityRankingRedisRepository;
import com.m3pro.groundflip.repository.RankingHistoryRepository;
import com.m3pro.groundflip.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityRankingService {
	private final CommunityRankingRedisRepository communityRankingRedisRepository;
	private final UserRepository userRepository;
	private final RankingHistoryRepository rankingHistoryRepository;
	
}
