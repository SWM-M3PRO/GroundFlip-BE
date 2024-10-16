package com.m3pro.groundflip.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.user.FcmTokenRequest;
import com.m3pro.groundflip.domain.entity.FcmToken;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.enums.Device;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.FcmTokenRepository;
import com.m3pro.groundflip.repository.UserActivityLogRepository;
import com.m3pro.groundflip.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FcmServiceTest {
	private static final Long testUserId = 1L;
	private static final String testFcmToken = "test token";
	private static FcmTokenRequest fcmTokenRequest;
	@Mock
	private UserRepository userRepository;
	@Mock
	private FcmTokenRepository fcmTokenRepository;
	@Mock
	private UserActivityLogRepository userActivityLogRepository;
	@InjectMocks
	private FcmService fcmService;

	@BeforeAll
	static void beforeAll() {
		fcmTokenRequest = new FcmTokenRequest(testUserId, testFcmToken, Device.IOS);
	}

	@Test
	@DisplayName("[registerFcmToken] user 가 없는 경우 에러 발생")
	void registerFcmToken_UserNotFound() {
		when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

		AppException exception = assertThrows(AppException.class, () -> fcmService.registerFcmToken(fcmTokenRequest));
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("[registerFcmToken] fcm 토큰 새로 등록")
	void registerFcmToken_RegisterNewToken() {
		User user = User.builder().id(1L).email("test@test.com").build();

		when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
		when(fcmTokenRepository.findByUser(user)).thenReturn(Optional.empty());

		fcmService.registerFcmToken(fcmTokenRequest);

		verify(fcmTokenRepository, times(1)).save(any());
		verify(userActivityLogRepository, times(1)).save(any());
	}

	@Test
	@DisplayName("[registerFcmToken] fcm 토큰이 이미 등록된 경우 수정 날짜만 변경")
	void registerFcmToken_UpdateModifiedDate() {
		User user = User.builder().id(1L).email("test@test.com").build();
		FcmToken fcmToken = FcmToken.builder().id(1L).token(testFcmToken).user(user).build();
		when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
		when(fcmTokenRepository.findByUser(user)).thenReturn(Optional.of(fcmToken));

		fcmService.registerFcmToken(fcmTokenRequest);

		assertThat(fcmToken.getModifiedAt()).isNotNull();
		verify(userActivityLogRepository, times(1)).save(any());
	}
}