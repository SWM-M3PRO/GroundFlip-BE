package com.m3pro.groundflip.domain.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(title = "로그인 요청 Body")
public class LoginRequest {
    @Schema(description = "프로바이더로부터 받은 액세스 토큰", example = "dslafjkdsrtjlejldfkajlasljdf")
    private String accessToken;
}
