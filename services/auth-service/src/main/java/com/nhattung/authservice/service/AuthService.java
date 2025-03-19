package com.nhattung.authservice.service;


import com.nhattung.authservice.repository.KeyCloakClient;
import com.nhattung.authservice.request.*;
import com.nhattung.authservice.response.AuthResponse;
import com.nhattung.authservice.response.LogoutResponse;
import com.nhattung.authservice.response.RefreshTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService{

    private final KeyCloakClient keyCloakClient;

    @Value("${idp.client.id}")
    private String CLIENT_ID;

    @Value("${idp.client.secret}")
    private String CLIENT_SECRET;

    @Value("${idp.client.scope}")
    private String SCOPE;

    @Value("${idp.client.grant-type}")
    private String GRANT_TYPE;


    @Override
    public AuthResponse exchangeToken(LoginRequest request) {
        return keyCloakClient.exchangeToken(TokenExchangeParam.builder()
                .client_id(CLIENT_ID)
                .client_secret(CLIENT_SECRET)
                .username(request.getUsername())
                .password(request.getPassword())
                .grant_type(GRANT_TYPE)
                .scope(SCOPE)
                .build());

    }

    @Override
    public LogoutResponse logout(RefreshRequest request) {
        return keyCloakClient.logout(LogoutParam.builder()
                .client_id(CLIENT_ID)
                .client_secret(CLIENT_SECRET)
                .refresh_token(request.getRefreshToken())
                .build());
    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshRequest request) {
        return keyCloakClient.refreshToken(RefreshTokenParam.builder()
                .client_id(CLIENT_ID)
                .client_secret(CLIENT_SECRET)
                .refresh_token(request.getRefreshToken())
                .build());
    }
}
