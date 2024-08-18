package com.m3pro.groundflip.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.m3pro.groundflip.domain.dto.permission.PermissionRequest;
import com.m3pro.groundflip.domain.dto.permission.PermissionResponse;
import com.m3pro.groundflip.domain.entity.Permission;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.repository.PermissionRepository;
import com.m3pro.groundflip.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {
	@Mock
	private UserRepository userRepository;

	@Mock
	private PermissionRepository permissionRepository;

	@InjectMocks
	private PermissionService permissionService;

	private User mockUser;
	private Permission mockPermission;

	@BeforeEach
	void setUp() {
		mockUser = User.builder()
			.id(1L)
			.nickname("testUser")
			.build();

		mockPermission = Permission.builder()
			.serviceNotificationsEnabled(true)
			.marketingNotificationsEnabled(true)
			.user(mockUser)
			.build();
	}

	@Test
	@DisplayName("[getAllPermissions] userId에 해당하는 권한의 정보를 가져온다.")
	void testGetAllPermissions() {
		when(userRepository.getReferenceById(anyLong())).thenReturn(mockUser);
		when(permissionRepository.findByUser(any(User.class))).thenReturn(Optional.of(mockPermission));

		PermissionResponse response = permissionService.getAllPermissions(1L);

		assertTrue(response.isServiceNotificationEnabled());
		assertTrue(response.isMarketingNotificationEnabled());

		verify(userRepository, times(1)).getReferenceById(1L);
		verify(permissionRepository, times(1)).findByUser(mockUser);
	}

	@Test
	@DisplayName("[updateServiceNotificationsPreference] service 권한을 정상적으로 변경한다.")
	void testUpdateServiceNotificationsPreference() {
		PermissionRequest request = new PermissionRequest(1L, false);

		when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
		when(permissionRepository.findByUser(any(User.class))).thenReturn(Optional.of(mockPermission));

		permissionService.updateServiceNotificationsPreference(request);

		verify(permissionRepository, times(1)).findByUser(mockUser);
		assertEquals(false, mockPermission.getServiceNotificationsEnabled());
	}

	@Test
	@DisplayName("[updateMarketingNotificationsPreference] marketing 권한을 정상적으로 변경한다.")
	void testUpdateMarketingNotificationsPreference() {
		PermissionRequest request = new PermissionRequest(1L, false);

		when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
		when(permissionRepository.findByUser(any(User.class))).thenReturn(Optional.of(mockPermission));

		permissionService.updateMarketingNotificationsPreference(request);

		verify(permissionRepository, times(1)).findByUser(mockUser);
		assertEquals(false, mockPermission.getMarketingNotificationsEnabled());
	}
}