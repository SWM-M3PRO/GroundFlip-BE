package com.m3pro.groundflip.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.community.ListResponseCommunity;
import com.m3pro.groundflip.domain.dto.community.ResponseGetGroup;
import com.m3pro.groundflip.domain.entity.Community;
import com.m3pro.groundflip.repository.CommunityRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityService {

	private final CommunityRepository communityRepository;

	public List<ListResponseCommunity> findCommunityByName(String name) {
		List<Community> community = communityRepository.findByNameLike("%" + name + "%");
		return community.stream().map(ListResponseCommunity::from).toList();
	}

	public ResponseGetGroup findCommunityById(Long id) {
		Community community = communityRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Community not found"));
		return ResponseGetGroup.from(community, 0, 0);
	}
}
