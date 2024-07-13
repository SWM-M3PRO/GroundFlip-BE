package com.m3pro.groundflip.service;

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
		String updateNickname = userInfoRequest.getNickname();

		if (!user.getNickname().equals(updateNickname) && userRepository.findByNickname(updateNickname).isPresent()) {
			throw new AppException(ErrorCode.DUPLICATED_NICKNAME);
		}

		user.updateGender(userInfoRequest.getGender());
		user.updateBirthYear(userInfoRequest.getBirthYear());
		user.updateNickName(userInfoRequest.getNickname());
		//user.updateProfileImage(userInfoRequest.getProfileImageUrl());
		user.updateStatus(UserStatus.COMPLETE);
		userRepository.save(user);
	}

}
