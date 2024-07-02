package com.m3pro.groundflip.domain.dto.pixel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class VisitedUserInfo {
	private String nickname;
	private String profileImageUrl;
}
