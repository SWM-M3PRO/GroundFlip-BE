package com.m3pro.groundflip.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.community.CommunitySearchListResponse;
import com.m3pro.groundflip.domain.dto.community.CommunityInfoResponse;
import com.m3pro.groundflip.domain.entity.Community;
import com.m3pro.groundflip.repository.CommunityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityService {

	private final CommunityRepository communityRepository;

	public List<CommunitySearchListResponse> findCommunityByName(String name) {
		List<Community> community = communityRepository.findAllByNameLike("%" + name + "%");
		return community.stream().map(CommunitySearchListResponse::from).toList();
	}

	public CommunityInfoResponse findCommunityById(Long id) {
		Community community = communityRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Community not found"));
		return CommunityInfoResponse.from(community, 0, 0);
	}
}
