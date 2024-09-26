package com.m3pro.groundflip.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.community.CommunityInfoResponse;
import com.m3pro.groundflip.domain.dto.community.CommunitySearchResponse;
import com.m3pro.groundflip.domain.dto.community.CommunitySignRequest;
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
	private final CommunityRankingService communityRankingService;
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
			.orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));
		Long memberCount = getMemberCount(community);

		Long rank = communityRankingService.getCommunityCurrentPixelRankFromCache(communityId);
		Long currentPixel = communityRankingService.getCurrentPixelCountFromCache(communityId);
		Long accumulatePixel = communityRankingService.getAccumulatePixelCount(communityId);

		return CommunityInfoResponse.from(community, rank, memberCount, currentPixel, accumulatePixel);
	}

	public void signInCommunity(Long communityId, CommunitySignRequest communitySignRequest) {
		User user = userRepository.findById(communitySignRequest.getUserId())
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

	public void signOutCommunity(Long communityId, CommunitySignRequest communitySignRequest) {
		User user = userRepository.findById(communitySignRequest.getUserId())
			.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

		Community community = communityRepository.findById(communityId)
			.orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));

		UserCommunity userCommunity = userCommunityRepository.findByUserAndCommunityAndDeletedAtIsNull(user, community)
			.orElseThrow(() -> new AppException(ErrorCode.ALREADY_SIGNED_OUT));

		userCommunity.updateDeletedAt(LocalDateTime.now());

		userCommunityRepository.save(userCommunity);
	}

	private Long getMemberCount(Community community) {
		return userCommunityRepository.countByCommunityId(community.getId());
	}
}
