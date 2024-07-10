package com.m3pro.groundflip.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.StepRecord.UserStepInfo;
import com.m3pro.groundflip.domain.dto.user.UserInfoResponse;
import com.m3pro.groundflip.domain.entity.StepRecord;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.domain.entity.UserCommunity;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.StepRecordRepository;
import com.m3pro.groundflip.repository.UserCommunityRepository;
import com.m3pro.groundflip.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final UserCommunityRepository userCommunityRepository;
	private final StepRecordRepository stepRecordRepository;

	public UserInfoResponse getUserInfo(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

		List<UserCommunity> userCommunity = userCommunityRepository.findByUserId(userId);

		if (userCommunity.isEmpty()) {
			return UserInfoResponse.from(user, null, null);
		} else {
			Long communityId = userCommunity.get(0).getCommunity().getId();
			String communityName = userCommunity.get(0).getCommunity().getName();
			return UserInfoResponse.from(user, communityId, communityName);
		}
	}

	@Transactional
	public void postUserStep(UserStepInfo userStepInfo) {
		User user = userRepository.findById(userStepInfo.getUserId())
			.orElseThrow(() -> new AppException(ErrorCode.PIXEL_NOT_FOUND));

		stepRecordRepository.save(
			StepRecord.builder()
				.user(user)
				.steps(userStepInfo.getSteps())
				.date(userStepInfo.getDate())
				.build()
		);

	}

}
