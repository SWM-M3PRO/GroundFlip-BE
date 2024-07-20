package com.m3pro.groundflip.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.user.UserInfoResponse;
import com.m3pro.groundflip.domain.entity.Community;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.domain.entity.UserCommunity;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.UserCommunityRepository;
import com.m3pro.groundflip.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	@Mock
	private UserRepository userRepository;

	@Mock
	private UserCommunityRepository userCommunityRepository;

	@InjectMocks
	private UserService userService;

	@Test
	@DisplayName("[getUserInfo] 존재하지 않는 user 를 조회하는 경우")
	void getUserInfoNotFoundTest() {
		// Given
		when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

		//When
		AppException exception = assertThrows(AppException.class, () -> userService.getUserInfo(1L));

		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("[getUserInfo] 그룹이 없는 사용자를 조회하는 경우")
	void getUserInfoNoGroupTest() throws ParseException {
		// Given
		String dateString = "2000-12-27";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date = formatter.parse(dateString);
		User user = User.builder().id(1L).birthYear(date).build();
		when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
		when(userCommunityRepository.findByUserId(anyLong())).thenReturn(new ArrayList<>());

		//When
		UserInfoResponse userInfoResponse = userService.getUserInfo(1L);

		//Then
		assertNull(userInfoResponse.getCommunityId());
		assertNull(userInfoResponse.getCommunityName());
		assertThat(userInfoResponse.getBirthYear()).isEqualTo(2000);
	}

	@Test
	@DisplayName("[getUserInfo] 그룹이 있는 사용자를 조회하는 경우")
	void getUserInfoTest() throws ParseException {
		// Given
		String dateString = "2000-12-27";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date = formatter.parse(dateString);
		User user = User.builder().id(1L).birthYear(date).build();
		Community community = Community.builder().id(1L).name("test").build();
		UserCommunity userCommunity = UserCommunity.builder()
			.user(user)
			.community(community)
			.build();
		when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
		when(userCommunityRepository.findByUserId(anyLong())).thenReturn(Collections.singletonList(userCommunity));

		//When
		UserInfoResponse userInfoResponse = userService.getUserInfo(1L);

		//Then
		assertThat(userInfoResponse.getCommunityId()).isEqualTo(1L);
		assertThat(userInfoResponse.getCommunityName()).isEqualTo("test");
	}
}
