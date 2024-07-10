package com.m3pro.groundflip.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(title = "로그인 응답")
public class LoginResponse {
    @Schema(description = "서버에서 발급한 액세스 토큰")
    private String accessToken;

    @Schema(description = "서버에서 발급한 리프레쉬 토큰")
    private String refreshToken;

    @Schema(description = "최초 로그인(회원가입)인지 여부", example = "true")
    private boolean isSignUp;
}
