package com.m3pro.groundflip.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.community.CommunityInfoResponse;
import com.m3pro.groundflip.domain.dto.community.CommunitySearchResponse;
import com.m3pro.groundflip.domain.entity.Community;
import com.m3pro.groundflip.repository.CommunityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityService {

	private final CommunityRepository communityRepository;

	/*
	 * 그룹명을 검색한다
	 * @param 그룹 name String
	 * @return 해당 String이 포함된 모든 그룹 name List
	 * */
	public List<CommunitySearchResponse> findAllCommunityByName(String name) {
		List<Community> community = communityRepository.findAllByNameLike("%" + name + "%");
		return community.stream().map(CommunitySearchResponse::from).toList();
	}

	public CommunityInfoResponse findCommunityById(Long id) {
		Community community = communityRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Community not found"));
		return CommunityInfoResponse.from(community, 0, 0);
	}
}
