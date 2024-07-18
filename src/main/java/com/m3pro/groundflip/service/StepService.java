package com.m3pro.groundflip.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.stepRecord.UserStepInfo;
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

	public List<Integer> getUserStepWhileWeek(Long userId, Date startDate, Date endDate) {
		List<StepRecord> stepRecordList = stepRecordRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

		Map<Date, Integer> stepsMap = stepRecordList.stream()
			.collect(Collectors.toMap(StepRecord::getDate, StepRecord::getSteps));

		List<Integer> result = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);

		while (!calendar.getTime().after(endDate)) {
			Date currentDate = truncateTime(calendar.getTime());
			int steps = stepsMap.getOrDefault(currentDate, 0);
			result.add(steps);
			calendar.add(Calendar.DATE, 1);
		}

		return result;
	}

	private Date truncateTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
}
