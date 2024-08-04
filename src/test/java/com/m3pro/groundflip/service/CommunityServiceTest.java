package com.m3pro.groundflip.service;

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
import com.m3pro.groundflip.domain.dto.community.CommunitySearchResponse;
import com.m3pro.groundflip.domain.entity.Community;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.CommunityRepository;

class CommunityServiceTest {
	@InjectMocks
	private CommunityService communityService;

	@Mock
	private CommunityRepository communityRepository;

	private Community community;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		// Community 객체 생성
		community = Community.builder()
			.id(1L)
			.name("Test Community")
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

		// When
		CommunityInfoResponse result = communityService.findCommunityById(communityId);

		// Then
		assertNotNull(result);
		assertEquals("Test Community", result.getName());
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
}