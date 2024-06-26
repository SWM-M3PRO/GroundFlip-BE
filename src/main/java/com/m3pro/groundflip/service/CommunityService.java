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

	//그룹 검색 api
	public List<ListResponseCommunity> findCommunityByName(String name){
		List<Community> community = communityRepository.findByNameLike("%"+name+"%");
		return community.stream().map(ListResponseCommunity::from).toList();
	}

	// 그룹 정보 get api인데 일단 검색만 하면 되는걸 깜빡하고 작성해버렸습니다.
	// 랭킹, 멤버카운트는 더미로 0을 넣어놨습니다. 나중에 계산해서 값을 넣을 수 있을 듯 합니다.
	public ResponseGetGroup findCommunityById(Long id){
		Community community = communityRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Community not found"));
		return ResponseGetGroup.from(community, 0,0);
	}
}
