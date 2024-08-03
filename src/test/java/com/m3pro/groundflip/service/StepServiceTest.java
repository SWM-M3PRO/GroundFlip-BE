package com.m3pro.groundflip.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.m3pro.groundflip.domain.dto.steprecord.UserStepInfo;
import com.m3pro.groundflip.domain.entity.StepRecord;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.repository.StepRecordRepository;
import com.m3pro.groundflip.repository.UserRepository;

class StepServiceTest {

	@InjectMocks
	private StepService stepService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private StepRecordRepository stepRecordRepository;

	private User user;
	private UserStepInfo userStepInfo;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		user = User.builder()
			.id(1L)
			.nickname("Test User")
			.build();

		userStepInfo = new UserStepInfo();
		userStepInfo.setUserId(user.getId());
		userStepInfo.setSteps(1000);
		userStepInfo.setDate(new java.sql.Date(2024, 10, 2));

		StepRecord stepRecord = StepRecord.builder()
			.user(user)
			.steps(1000)
			.date(userStepInfo.getDate())
			.build();

		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(stepRecordRepository.save(any(StepRecord.class))).thenReturn(stepRecord);
	}

	@Test
	@DisplayName("[postUserStep] 정상적으로 걸음수 저장")
	void postUserStep_shouldSaveStepRecord() {
		// When
		stepService.postUserStep(userStepInfo);

		// Then
		verify(stepRecordRepository, times(1)).save(any(StepRecord.class));
	}

	@Test
	@DisplayName("[postUserStep] 없는 유저의 걸음수를 저장하는 경우 에러")
	void postUserStep_shouldThrowExceptionWhenUserNotFound() {
		// Given
		when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

		// When / Then
		assertThrows(AppException.class, () -> stepService.postUserStep(userStepInfo));
	}

	@Test
	@DisplayName("[getUserStepWhileWeek] startDate, endDate 사이의 걸음수를 반환, 없는 경우 0으로 반환")
	void getUserStepWhileWeek_shouldReturnListOfSteps() {
		// Given
		java.sql.Date startDate = new java.sql.Date(2024, 10, 2);
		java.sql.Date endDate = new java.sql.Date(2024, 10, 8);

		List<StepRecord> mockStepRecords = Arrays.asList(
			StepRecord.builder().user(user).steps(1000).date(startDate).build(),
			StepRecord.builder().user(user).steps(2000).date(endDate).build()
		);

		when(stepRecordRepository.findByUserIdAndDateBetween(user.getId(), startDate, endDate))
			.thenReturn(mockStepRecords);

		// When
		List<Integer> steps = stepService.getUserStepWhileWeek(user.getId(), startDate, endDate);

		// Then
		assertEquals(7, steps.size());
		assertEquals(1000, steps.get(0));
		assertEquals(2000, steps.get(6));
	}
}
