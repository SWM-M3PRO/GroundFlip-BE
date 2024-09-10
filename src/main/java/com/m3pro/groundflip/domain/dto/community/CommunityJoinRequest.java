package com.m3pro.groundflip.domain.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(title = "그룹 가입 정보")
public class CommunityJoinRequest {

	@Schema(description = "가입 유저 id", example = "1")
	private Long userId;
}
