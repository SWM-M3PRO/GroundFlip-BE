package com.m3pro.groundflip.controller;

import com.m3pro.groundflip.domain.dto.Response;
import com.m3pro.groundflip.domain.dto.auth.LoginRequest;
import com.m3pro.groundflip.domain.dto.auth.LoginResponse;
import com.m3pro.groundflip.enums.Provider;
import com.m3pro.groundflip.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/kakao/login")
    public Response<LoginResponse> loginKaKao(@RequestBody LoginRequest loginRequest) {
        return Response.createSuccess(authService.login(Provider.KAKAO, loginRequest));
    }
}
