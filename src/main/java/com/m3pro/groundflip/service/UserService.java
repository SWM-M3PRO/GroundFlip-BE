package com.m3pro.groundflip.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.m3pro.groundflip.domain.dto.user.UserInfoRequest;
import com.m3pro.groundflip.domain.dto.user.UserInfoResponse;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.domain.entity.UserCommunity;
import com.m3pro.groundflip.enums.UserStatus;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.UserCommunityRepository;
import com.m3pro.groundflip.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final UserCommunityRepository userCommunityRepository;

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

	public void putUserInfo(Long userId, UserInfoRequest userInfoRequest) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

		if (checkNicknameExists(userInfoRequest, user)) {
			throw new AppException(ErrorCode.DUPLICATED_NICKNAME);
		}

		user.updateGender(userInfoRequest.getGender());
		user.updateBirthYear(convertToDate(userInfoRequest.getBirthYear()));
		user.updateNickName(userInfoRequest.getNickname());
		user.updateProfileImage(userInfoRequest.getProfileImageUrl());
		user.updateStatus(UserStatus.COMPLETE);
		userRepository.save(user);
	}

	public Date convertToDate(int year) {
		LocalDate localDate = LocalDate.of(year, 1, 1);
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	public boolean checkNicknameExists(UserInfoRequest userInfoRequest, User user) {
		boolean duplicate = false;
		if(userRepository.findByNickname(userInfoRequest.getNickname()).isPresent()) {
			duplicate = true;
		}
		return duplicate;
	}
}
