package com.m3pro.groundflip.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.StepRecord.UserStepInfo;
import com.m3pro.groundflip.domain.entity.StepRecord;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.StepRecordRepository;
import com.m3pro.groundflip.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StepService {
	private final UserRepository userRepository;
	private final StepRecordRepository stepRecordRepository;

	@Transactional
	public void postUserStep(UserStepInfo userStepInfo) {
		User user = userRepository.findById(userStepInfo.getUserId())
			.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

		stepRecordRepository.save(
			StepRecord.builder()
				.user(user)
				.steps(userStepInfo.getSteps())
				.date(userStepInfo.getDate())
				.build()
		);
	}

	public List<Integer> getUserStep(Long userId, Date startDate, Date endDate) {
		return stepRecordRepository.findByUserIdStartEndDate(userId, startDate, endDate);
	}
}
