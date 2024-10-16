package com.m3pro.groundflip.domain.dto.community;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "그룹 생성")
public class CommunityInfoRequest {

	@Schema(description = "그룹명", example = "그라운드플립")
	private String name;

	@Schema(description = "그룹 프로필 사진 파일", example = "사진 파일")
	private MultipartFile profileImage;

	@Schema(description = "그룹 색상", example = "0xff123456")
	private String communityColor;

	@Schema(description = "그룹 비밀번호", example = "1234")
	private String password;
}
