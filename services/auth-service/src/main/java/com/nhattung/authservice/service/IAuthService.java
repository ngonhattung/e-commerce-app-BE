package com.nhattung.authservice.service;

import com.nhattung.authservice.request.LoginRequest;
import com.nhattung.authservice.request.RefreshRequest;
import com.nhattung.authservice.response.AuthResponse;
import com.nhattung.authservice.response.LogoutResponse;
import com.nhattung.authservice.response.RefreshTokenResponse;

public interface IAuthService {

    AuthResponse exchangeToken(LoginRequest request);
    LogoutResponse logout(RefreshRequest request);
    RefreshTokenResponse refreshToken(RefreshRequest request);
}
