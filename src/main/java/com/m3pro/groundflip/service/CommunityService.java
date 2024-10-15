package com.m3pro.groundflip.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.m3pro.groundflip.domain.dto.community.CommunityInfoRequest;
import com.m3pro.groundflip.domain.dto.community.CommunityInfoResponse;
import com.m3pro.groundflip.domain.dto.community.CommunitySearchResponse;
import com.m3pro.groundflip.domain.dto.community.CommunitySignRequest;
import com.m3pro.groundflip.domain.dto.ranking.Ranking;
import com.m3pro.groundflip.domain.dto.ranking.UserRankingResponse;
import com.m3pro.groundflip.domain.entity.Community;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.domain.entity.UserCommunity;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.CommunityRankingRedisRepository;
import com.m3pro.groundflip.repository.CommunityRepository;
import com.m3pro.groundflip.repository.UserCommunityRepository;
import com.m3pro.groundflip.repository.UserRankingRedisRepository;
import com.m3pro.groundflip.repository.UserRepository;
import com.m3pro.groundflip.util.S3Uploader;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityService {
	private final CommunityRepository communityRepository;
	private final UserCommunityRepository userCommunityRepository;
	private final CommunityRankingService communityRankingService;
	private final UserRepository userRepository;
	private final UserRankingRedisRepository userRankingRedisRepository;
	private final CommunityRankingRedisRepository communityRankingRedisRepository;
	private final S3Uploader s3Uploader;

	/*
	 * 그룹명을 검색한다
	 * @param 그룹 name String
	 * @return 해당 String이 포함된 모든 그룹 name List
	 * */
	public List<CommunitySearchResponse> getCommunitiesByName(String name) {
		List<Community> community = communityRepository.findAllByNameLike("%" + name + "%");
		return community.stream().map(CommunitySearchResponse::from).toList();
	}

	public CommunityInfoResponse getCommunityInfo(Long communityId) {
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

	public List<UserRankingResponse> getCommunityMembers(Long communityId, int count) {
		List<User> members = userCommunityRepository.findUsersByCommunityId(communityId);
		List<Ranking> rankings = userRankingRedisRepository.getRankingsWithCurrentPixelCount(-1);

		Map<Long, User> memberMap = new HashMap<>();
		members.forEach((member) -> memberMap.put(member.getId(), member));
		List<UserRankingResponse> userRankingResponses = new ArrayList<>();
		long rank = 1L;
		int totalCount = 0;

		for (Ranking ranking : rankings) {
			if (memberMap.containsKey(ranking.getId())) {
				userRankingResponses.add(
					UserRankingResponse
						.from(memberMap.get(ranking.getId()), rank, ranking.getCurrentPixelCount())
				);
				totalCount += 1;
				rank += 1;
				if (totalCount == count) {
					break;
				}
			}
		}
		return userRankingResponses;
	}

	@Transactional
	public void createCommunity(CommunityInfoRequest communityInfoRequest, MultipartFile multipartFile) throws
		IOException {
		String fileS3Url;
		Long communityId;

		if (communityRepository.findByName(communityInfoRequest.getName()) != null) {
			throw new AppException(ErrorCode.DUPLICATED_NICKNAME);
		}

		fileS3Url = s3Uploader.uploadCommunityFiles(multipartFile);

		communityRepository.save(
			Community.builder()
				.name(communityInfoRequest.getName())
				.backgroundImageUrl(fileS3Url)
				.communityColor(communityInfoRequest.getCommunityColor())
				.password(communityInfoRequest.getPassword())
				.maxRanking(0)
				.maxPixelCount(0)
				.build()
		);
		communityId = communityRepository.findByName(communityInfoRequest.getName()).getId();
		communityRankingRedisRepository.saveCommunityInRanking(communityId);
	}
}
