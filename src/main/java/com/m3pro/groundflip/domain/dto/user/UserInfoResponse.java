package com.m3pro.groundflip.domain.dto.user;

import java.util.Date;

import com.m3pro.groundflip.domain.entity.User;
import com.m3pro.groundflip.enums.Gender;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(title = "사용자 정보")
public class UserInfoResponse {
	@Schema(description = "사용자 ID", example = "1234")
	private Long userId;

	@Schema(description = "사용자 닉네임", example = "홍길동")
	private String nickname;

	@Schema(description = "사용자 프로필 사진 주소", example = "http://www.test.com")
	private String profileImageUrl;

	@Schema(description = "그룹 ID", example = "123")
	private Long communityId;

	@Schema(description = "그룹 이름", example = "홍익대학교")
	private String communityName;

	@Schema(description = "사용자 출생년도", example = "2000")
	private Date birthYear;

	@Schema(description = "사용자 성별", example = "남성")
	private Gender gender;

	public static UserInfoResponse from(User user, Long communityId, String communityName) {
		return UserInfoResponse.builder()
			.userId(user.getId())
			.nickname(user.getNickname())
			.profileImageUrl(user.getProfileImage())
			.gender(user.getGender())
			.communityId(communityId)
			.communityName(communityName)
			.build();
	}
}
