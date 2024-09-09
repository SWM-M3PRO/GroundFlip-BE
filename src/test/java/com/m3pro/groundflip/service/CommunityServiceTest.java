package com.m3pro.groundflip.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

class CommunityServiceTest {
	@InjectMocks
	private CommunityService communityService;

	@Mock
	private CommunityRepository communityRepository;

	@Mock
	private UserCommunityRepository userCommunityRepository;

	@Mock
	private UserRepository userRepository;

	private Community community;

	private User user;

	private UserCommunity userCommunity;

	private CommunityJoinRequest communityJoinRequest;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		// Community 객체 생성
		community = Community.builder()
			.id(1L)
			.name("Test Community")
			.communityColor("0xFF0DF69E")
			.maxRanking(3)
			.maxPixelCount(54)
			.backgroundImageUrl("www.test.com")
			.build();

		user = User.builder()
			.id(1L)
			.nickname("Test User")
			.build();

		userCommunity = UserCommunity.builder()
			.community(community)
			.user(user)
			.build();
	}

	@Test
	@DisplayName("[findAllCommunityByName_] searchName 과 일치하는 모든 그룹 정보 반환")
	void findAllCommunityByName_shouldReturnMatchingCommunities() {
		// Given
		String searchName = "Test";
		when(communityRepository.findAllByNameLike("%" + searchName + "%"))
			.thenReturn(List.of(community));

		// When
		List<CommunitySearchResponse> results = communityService.findAllCommunityByName(searchName);

		// Then
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals("Test Community", results.get(0).getName());
	}

	@Test
	@DisplayName("[findCommunityById] 그룹 아이디에 해당하는 그룹 정보 반환")
	void findCommunityById_shouldReturnCommunityInfoResponse() {
		// Given
		Long communityId = 1L;
		when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
		when(userCommunityRepository.countByCommunityId(communityId)).thenReturn(3L);

		// When
		CommunityInfoResponse result = communityService.findCommunityById(communityId);

		// Then
		assertNotNull(result);
		assertEquals("Test Community", result.getName());
		assertThat(result.getMemberCount()).isEqualTo(3);
		assertThat(result.getAccumulatePixelCount()).isEqualTo(0);
		assertThat(result.getCurrentPixelCount()).isEqualTo(0);
		assertThat(result.getCommunityRanking()).isEqualTo(0);
	}

	@Test
	@DisplayName("[findCommunityById] 없는 그룹인 경우 에러")
	void findCommunityById_shouldThrowExceptionWhenCommunityNotFound() {
		// Given
		Long communityId = 2L;
		when(communityRepository.findById(communityId)).thenReturn(Optional.empty());

		// When & Then
		AppException exception = assertThrows(AppException.class,
			() -> communityService.findCommunityById(communityId));
		assertEquals(ErrorCode.GROUP_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("[joinCommunity] 그룹 가입 테스트")
	void testJoinCommunity() {
		//Given
		Long communityId = 1L;
		Long userId = 1L;

		when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		communityJoinRequest = CommunityJoinRequest.builder()
			.userId(userId)
			.build();

		//When
		communityService.joinCommunity(communityId, communityJoinRequest);

		//Then
		verify(userCommunityRepository).save(any(UserCommunity.class));
	}

	@Test
	@DisplayName("[joinCommunity] 유저가 없을때 에러 테스트")
	void testJoinCommunity_userNotFound() {
		// Given
		CommunityJoinRequest communityJoinRequest2 = CommunityJoinRequest.builder()
			.userId(2L)
			.build();

		// when
		AppException thrown = assertThrows(AppException.class, () -> {
			communityService.joinCommunity(2L, communityJoinRequest2);
		});

		// Then
		assertEquals(ErrorCode.USER_NOT_FOUND, thrown.getErrorCode());
	}

	@Test
	@DisplayName("[joinCommunity] 그룹이 없을때 에러 테스트")
	void testJoinCommunity_communityNotFound() {
		// Given
		when(communityRepository.findById(2L)).thenReturn(Optional.empty());
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		communityJoinRequest = CommunityJoinRequest.builder()
			.userId(1L)
			.build();

		// when
		AppException thrown = assertThrows(AppException.class, () -> {
			communityService.joinCommunity(2L, communityJoinRequest);
		});

		// Then
		assertEquals(ErrorCode.COMMUNITY_NOT_FOUND, thrown.getErrorCode());
	}

	@Test
	@DisplayName("[joinCommunity] 가입된 그룹이 이미 있을때 테스트")
	void testJoinCommunity_alreadyJoined() {
		// Given
		when(communityRepository.findById(1L)).thenReturn(Optional.of(community));
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userCommunityRepository.findByUser(user)).thenReturn(userCommunity);

		communityJoinRequest = CommunityJoinRequest.builder()
			.userId(1L)
			.build();

		// when
		AppException thrown = assertThrows(AppException.class, () -> {
			communityService.joinCommunity(1L, communityJoinRequest);
		});

		// Then
		assertEquals(ErrorCode.ALREADY_JOINED, thrown.getErrorCode());
	}

}