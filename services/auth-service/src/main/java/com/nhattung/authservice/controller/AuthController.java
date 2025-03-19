package com.nhattung.authservice.controller;

import com.nhattung.authservice.request.LoginRequest;
import com.nhattung.authservice.request.RefreshRequest;
import com.nhattung.authservice.response.ApiResponse;
import com.nhattung.authservice.response.AuthResponse;
import com.nhattung.authservice.response.LogoutResponse;
import com.nhattung.authservice.response.RefreshTokenResponse;
import com.nhattung.authservice.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.<AuthResponse>builder()
                .result(authService.exchangeToken(LoginRequest.builder()
                        .username(request.getUsername())
                        .password(request.getPassword())
                        .build()))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout(@RequestBody RefreshRequest request) {
        return ApiResponse.<LogoutResponse>builder()
                .result(authService.logout(RefreshRequest.builder()
                        .refreshToken(request.getRefreshToken())
                        .build()))
                .message("Logout success")
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refresh(@RequestBody RefreshRequest request) {
        return ApiResponse.<RefreshTokenResponse>builder()
                .result(authService.refreshToken(RefreshRequest.builder()
                        .refreshToken(request.getRefreshToken())
                        .build()))
                .build();
    }
}
