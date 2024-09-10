package com.m3pro.groundflip.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.community.CommunityInfoResponse;
import com.m3pro.groundflip.domain.dto.community.CommunityJoinRequest;
import com.m3pro.groundflip.domain.dto.community.CommunitySearchResponse;
import com.m3pro.groundflip.domain.entity.Community;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.domain.entity.UserCommunity;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.CommunityRepository;
import com.m3pro.groundflip.repository.UserCommunityRepository;
import com.m3pro.groundflip.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityService {
	private final CommunityRepository communityRepository;
	private final UserCommunityRepository userCommunityRepository;
	private final UserRepository userRepository;

	/*
	 * 그룹명을 검색한다
	 * @param 그룹 name String
	 * @return 해당 String이 포함된 모든 그룹 name List
	 * */
	public List<CommunitySearchResponse> findAllCommunityByName(String name) {
		List<Community> community = communityRepository.findAllByNameLike("%" + name + "%");
		return community.stream().map(CommunitySearchResponse::from).toList();
	}

	public CommunityInfoResponse findCommunityById(Long communityId) {
		Community community = communityRepository.findById(communityId)
			.orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));
		Long memberCount = getMemberCount(community);
		// ToDo : 랭킹 하시는 분이 구현하신 것 토대로 communityRanking, currentPixelCount, accumulatePixelCount만 채워주세요.
		return CommunityInfoResponse.from(community, 0, memberCount, 0L, 0L);
	}

	public void joinCommunity(Long communityId, CommunityJoinRequest communityJoinRequest) {
		User user = userRepository.findById(communityJoinRequest.getUserId())
			.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

		Community community = communityRepository.findById(communityId)
			.orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));

		Boolean isJoined = userCommunityRepository.existsByUserAndCommunityAndDeletedAtIsNull(user, community);

		if (isJoined) {
			throw new AppException(ErrorCode.ALREADY_JOINED);
		}

		UserCommunity userCommunity = UserCommunity.builder()
			.user(user)
			.community(community)
			.build();

		userCommunityRepository.save(userCommunity);
	}

	private Long getMemberCount(Community community) {
		return userCommunityRepository.countByCommunityId(community.getId());
	}
}
