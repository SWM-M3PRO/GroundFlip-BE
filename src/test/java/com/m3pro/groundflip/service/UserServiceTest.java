package com.m3pro.groundflip.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.user.UserDeleteRequest;
import com.m3pro.groundflip.domain.dto.user.UserInfoRequest;
import com.m3pro.groundflip.domain.dto.user.UserInfoResponse;
import com.m3pro.groundflip.domain.entity.AppleRefreshToken;
import com.m3pro.groundflip.domain.entity.Community;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.domain.entity.UserCommunity;
import com.m3pro.groundflip.enums.Gender;
import com.m3pro.groundflip.enums.Provider;
import com.m3pro.groundflip.enums.UserStatus;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.jwt.JwtProvider;
import com.m3pro.groundflip.repository.AppleRefreshTokenRepository;
import com.m3pro.groundflip.repository.FcmTokenRepository;
import com.m3pro.groundflip.repository.UserCommunityRepository;
import com.m3pro.groundflip.repository.UserRankingRedisRepository;
import com.m3pro.groundflip.repository.UserRepository;
import com.m3pro.groundflip.service.oauth.AppleApiClient;
import com.m3pro.groundflip.util.S3Uploader;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	@Mock
	private UserRepository userRepository;

	@Mock
	private UserRankingRedisRepository userRankingRedisRepository;

	@Mock
	private FcmTokenRepository fcmTokenRepository;

	@Mock
	private S3Uploader s3Uploader;

	@Mock
	private UserCommunityRepository userCommunityRepository;

	@Mock
	private AppleRefreshTokenRepository appleRefreshTokenRepository;

	@Mock
	private AppleApiClient appleApiClient;

	@Mock
	private JwtProvider jwtProvider;

	@InjectMocks
	private UserService userService;

	@Test
	@DisplayName("[PutUserInfo] user정보가 올바르게 업데이트 되는지")
	void putUserInfoTest() throws IOException {
		//Given
		Long userId = 1L;
		String exampleNickname = "kim";
		Gender gender = Gender.MALE;
		int year = 2000;
		LocalDate localDate = LocalDate.of(year, 1, 1);
		Date birthYear = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

		User user = User.builder().id(userId).nickname("before kim").build();

		UserInfoRequest userInfoRequest = UserInfoRequest.builder()
			.nickname(exampleNickname)
			.gender(gender)
			.birthYear(year)
			.build();

		//When
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		userService.putUserInfo(userId, userInfoRequest, null);

		//Then
		assertThat(user.getNickname()).isEqualTo(exampleNickname);
		assertThat(user.getBirthYear()).isEqualTo(birthYear);
		assertThat(user.getGender()).isEqualTo(gender);

	}

	@Test
	@DisplayName("[putUserInfo] 중복된 user닉네임을 설정하는 경우")
	void putUserInfoDuplicate() {
		// Given
		Long userId1 = 1L;
		Long userId2 = 2L;
		String userNickName1 = "kim";
		String userNickName2 = "cha";

		User user1 = User.builder()
			.id(userId1)
			.nickname(userNickName1)
			.build();

		User user2 = User.builder()
			.id(userId2)
			.nickname(userNickName2)
			.build();

		UserInfoRequest userInfoRequest = UserInfoRequest.builder()
			.nickname(userNickName2)
			.profileImage(null).build();

		//When
		when(userRepository.findById(userId1)).thenReturn(Optional.of(user1));
		when(userRepository.findByNickname(userNickName2)).thenReturn(Optional.of(user2));

		// Then
		AppException exception = assertThrows(AppException.class,
			() -> userService.putUserInfo(userId1, userInfoRequest, null));

		assertEquals(ErrorCode.DUPLICATED_NICKNAME, exception.getErrorCode());
		verify(userRepository, times(1)).findByNickname(userNickName2);
	}

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
		UserCommunity userCommunity = UserCommunity.builder().user(user).community(community).build();
		when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
		when(userCommunityRepository.findByUserId(anyLong())).thenReturn(Collections.singletonList(userCommunity));

		//When
		UserInfoResponse userInfoResponse = userService.getUserInfo(1L);

		//Then
		assertThat(userInfoResponse.getCommunityId()).isEqualTo(1L);
		assertThat(userInfoResponse.getCommunityName()).isEqualTo("test");
	}

	@Test
	@DisplayName("[deleteUser] 사용자의 정보가 정상적으로 masking 되는 지 확인한다.")
	void deleteUserTest() {
		User deleteUser = User.builder()
			.id(1L)
			.email("test1@naver.com")
			.provider(Provider.KAKAO)
			.gender(Gender.MALE)
			.birthYear(new Date())
			.profileImage("https://s3-fake-url")
			.status(UserStatus.COMPLETE)
			.nickname("test")
			.build();

		when(userRepository.findById(deleteUser.getId())).thenReturn(Optional.of(deleteUser));

		userService.deleteUser(1L, new UserDeleteRequest("acessToken", "refreshToken"));

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(deleteUser.getBirthYear());

		verify(fcmTokenRepository, times(1)).deleteByUser(any());
		assertThat(deleteUser.getNickname()).isEqualTo(null);
		assertThat(deleteUser.getProfileImage()).isEqualTo(null);
		assertThat(calendar.get(Calendar.YEAR)).isEqualTo(1900);
		assertThat(deleteUser.getEmail()).isEqualTo("unknown@unknown.com");

	}

	@Test
	@DisplayName("[deleteUser] 애플 로그인 경우 애플 토큰을 정상적으로 revoke 하고 사용자의 정보가 정상적으로 masking 되는 지 확인한다.")
	void deleteUserTestInApple() {
		User deleteUser = User.builder()
			.id(1L)
			.email("test1@naver.com")
			.provider(Provider.APPLE)
			.gender(Gender.MALE)
			.birthYear(new Date())
			.profileImage("https://s3-fake-url")
			.status(UserStatus.COMPLETE)
			.nickname("test")
			.build();

		when(userRepository.findById(deleteUser.getId())).thenReturn(Optional.of(deleteUser));
		when(appleRefreshTokenRepository.findByUserId(any())).thenReturn(
			Optional.of(AppleRefreshToken.builder().refreshToken("test").build()));

		userService.deleteUser(1L, new UserDeleteRequest("acessToken", "refreshToken"));

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(deleteUser.getBirthYear());
		verify(fcmTokenRepository, times(1)).deleteByUser(any());
		verify(appleApiClient, times(1)).revokeToken(any());
		verify(appleRefreshTokenRepository, times(1)).delete(any());
		assertThat(deleteUser.getNickname()).isEqualTo(null);
		assertThat(deleteUser.getProfileImage()).isEqualTo(null);
		assertThat(calendar.get(Calendar.YEAR)).isEqualTo(1900);
		assertThat(deleteUser.getEmail()).isEqualTo("unknown@unknown.com");

	}
}
