package com.m3pro.groundflip.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.m3pro.groundflip.domain.dto.user.UserDeleteRequest;
import com.m3pro.groundflip.domain.dto.user.UserInfoRequest;
import com.m3pro.groundflip.domain.dto.user.UserInfoResponse;
import com.m3pro.groundflip.domain.entity.AppleRefreshToken;
import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.domain.entity.UserCommunity;
import com.m3pro.groundflip.enums.Provider;
import com.m3pro.groundflip.enums.UserStatus;
import com.m3pro.groundflip.exception.AppException;
import com.m3pro.groundflip.exception.ErrorCode;
import com.m3pro.groundflip.jwt.JwtProvider;
import com.m3pro.groundflip.repository.AppleRefreshTokenRepository;
import com.m3pro.groundflip.repository.FcmTokenRepository;
import com.m3pro.groundflip.repository.RankingHistoryRepository;
import com.m3pro.groundflip.repository.UserCommunityRepository;
import com.m3pro.groundflip.repository.UserRankingRedisRepository;
import com.m3pro.groundflip.repository.UserRepository;
import com.m3pro.groundflip.service.oauth.AppleApiClient;
import com.m3pro.groundflip.util.DateUtils;
import com.m3pro.groundflip.util.S3Uploader;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
	private final RankingHistoryRepository rankingHistoryRepository;
	private final UserRankingRedisRepository userRankingRedisRepository;
	private final UserRepository userRepository;
	private final AppleRefreshTokenRepository appleRefreshTokenRepository;
	private final UserCommunityRepository userCommunityRepository;
	private final FcmTokenRepository fcmTokenRepository;
	private final S3Uploader s3Uploader;
	private final JwtProvider jwtProvider;
	private final AppleApiClient appleApiClient;

	@Value("${image.default}")
	private String defaultImagePath;

	/**
	 * 유저의 정보를 반환한다.
	 * @param userId 사용자 Id
	 * @return 사용자 ID, 사용자 닉네임, 사용자 프로필 사진 주소, 그룹 ID, 그룹 이름, 사용자 출생년도, 사용자 성별
	 */
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

	/**
	 * 유저의 정보를 수정한다
	 * @Param userId 유저id
	 * @Param userInfoRequest 유저정보dto (gender, year, nickname)
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
			fileS3Url = s3Uploader.uploadFiles(multipartFile, userId);
		} else {
			fileS3Url = defaultImagePath;
		}

		user.updateGender(userInfoRequest.getGender());
		user.updateBirthYear(convertToDate(userInfoRequest.getBirthYear()));
		user.updateNickName(userInfoRequest.getNickname());
		user.updateStatus(UserStatus.COMPLETE);
		user.updateProfileImage(fileS3Url);
		userRepository.save(user);
		userRankingRedisRepository.saveUserInRanking(user.getId());
	}

	private Date convertToDate(int year) {
		LocalDate localDate = LocalDate.of(year, 1, 1);
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	private boolean checkNicknameExists(UserInfoRequest userInfoRequest, User user) {
		boolean isDuplicate = false;
		if (!userInfoRequest.getNickname().equals(user.getNickname())) {
			if (userRepository.findByNickname(userInfoRequest.getNickname()).isPresent()) {
				isDuplicate = true;
			}
		}
		return isDuplicate;
	}

	@Transactional
	public void deleteUser(Long userId, UserDeleteRequest userDeleteRequest) {
		User deletedUser = userRepository.findById(userId)
			.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		if (deletedUser.getProvider() == Provider.APPLE) {
			revokeAppleToken(deletedUser.getId());
		}
		fcmTokenRepository.deleteByUser(deletedUser);

		deletedUser.updateBirthYear(convertToDate(1900));
		deletedUser.updateNickName(null);
		deletedUser.updateProfileImage(null);
		deletedUser.updateEmail("unknown@unknown.com");

		jwtProvider.expireToken(userDeleteRequest.getAccessToken());
		jwtProvider.expireToken(userDeleteRequest.getRefreshToken());

		int year = LocalDate.now().getYear();
		int week = DateUtils.getWeekOfDate(LocalDate.now());

		rankingHistoryRepository.deleteByUserIdAndYearAndWeek(userId, year, week);
		userRankingRedisRepository.deleteUserInRanking(userId);
	}

	private void revokeAppleToken(Long userId) {
		try {
			AppleRefreshToken appleRefreshToken = appleRefreshTokenRepository.findByUserId(userId)
				.orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR));
			appleApiClient.revokeToken(appleRefreshToken.getRefreshToken());
			appleRefreshTokenRepository.delete(appleRefreshToken);
		} catch (Exception e) {
			log.warn("revoke apple token failed userId : [{}]", userId);
		}
	}
}
