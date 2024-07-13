package com.m3pro.groundflip.domain.dto.user;

import java.util.Date;

import com.m3pro.groundflip.enums.Gender;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "사용자 정보 수정")
public class UserInfoRequest {

	@Schema(description = "사용자 닉네임", example = "홍길동")
	private String nickname;

	@Schema(description = "사용자 프로필 사진 주소", example = "http://www.test.com")
	private String profileImageUrl;

	@Schema(description = "사용자 출생년도", example = "2000")
	private int birthYear;

	@Schema(description = "사용자 성별 (MALE, FEMALE)", example = "MALE")
	private Gender gender;

}
