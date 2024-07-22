package com.m3pro.groundflip.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.m3pro.groundflip.domain.dto.user.UserInfoRequest;
import com.m3pro.groundflip.domain.dto.user.UserInfoResponse;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.domain.entity.UserCommunity;
import com.m3pro.groundflip.enums.UserStatus;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.repository.UserCommunityRepository;
import com.m3pro.groundflip.repository.UserRepository;
import com.m3pro.groundflip.util.S3Uploader;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final UserCommunityRepository userCommunityRepository;
	private final S3Uploader s3Uploader;

	public UserInfoResponse getUserInfo(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

		List<UserCommunity> userCommunity = userCommunityRepository.findByUserId(userId);

		LocalDate localDate = user.getBirthYear().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		int year = localDate.getYear();

		if (userCommunity.isEmpty()) {
			return UserInfoResponse.from(user, year, null, null);
		} else {
			Long communityId = userCommunity.get(0).getCommunity().getId();
			String communityName = userCommunity.get(0).getCommunity().getName();
			return UserInfoResponse.from(user, year, communityId, communityName);
		}
	}

	/*
	 * 유저의 정보를 수정한다
	 * @Param 유저id
	 * @Param 유저정보dto (gender, year, nickname)
	 * @Param 이미지 multipartFile
	 * convertToDate() int로 들어온 year을 Date로 변환
	 * checkNicknameExists() 닉네임 중복 체크를 위해 닉네임이 있는지 확인
	 * */
	@Transactional
	public void putUserInfo(Long userId, UserInfoRequest userInfoRequest, MultipartFile multipartFile) throws
		IOException {
		String fileS3Url;
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

		if (checkNicknameExists(userInfoRequest, user)) {
			throw new AppException(ErrorCode.DUPLICATED_NICKNAME);
		}

		if (multipartFile != null) {
			fileS3Url = s3Uploader.uploadFiles(multipartFile);
		} else {
			fileS3Url = user.getProfileImage();
		}

		user.updateGender(userInfoRequest.getGender());
		user.updateBirthYear(convertToDate(userInfoRequest.getBirthYear()));
		user.updateNickName(userInfoRequest.getNickname());
		user.updateProfileImage(fileS3Url);
		user.updateStatus(UserStatus.COMPLETE);
		userRepository.save(user);
	}

	public Date convertToDate(int year) {
		LocalDate localDate = LocalDate.of(year, 1, 1);
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	public boolean checkNicknameExists(UserInfoRequest userInfoRequest, User user) {
		boolean isDuplicate = false;
		if (!userInfoRequest.getNickname().equals(user.getNickname())) {
			if (userRepository.findByNickname(userInfoRequest.getNickname()).isPresent()) {
				isDuplicate = true;
			}
		}
		return isDuplicate;
	}
}
