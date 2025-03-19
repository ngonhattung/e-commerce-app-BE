package com.nhattung.authservice.service;


import com.nhattung.authservice.exception.AppException;
import com.nhattung.authservice.exception.ErrorCode;
import com.nhattung.authservice.repository.KeyCloakClient;
import com.nhattung.authservice.request.*;
import com.nhattung.authservice.response.AuthResponse;
import com.nhattung.authservice.response.LogoutResponse;
import com.nhattung.authservice.response.RefreshTokenResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
        try {
            return keyCloakClient.exchangeToken(TokenExchangeParam.builder()
                    .client_id(CLIENT_ID)
                    .client_secret(CLIENT_SECRET)
                    .username(request.getUsername().trim())
                    .password(request.getPassword())
                    .grant_type(GRANT_TYPE)
                    .scope(SCOPE)
                    .build());
        } catch (FeignException.Unauthorized e) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        } catch (FeignException e) {
            throw new AppException(ErrorCode.FEIGN_ERROR);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

    }

    @Override
    public LogoutResponse logout(RefreshRequest request) {
        try {
            return keyCloakClient.logout(LogoutParam.builder()
                    .client_id(CLIENT_ID)
                    .client_secret(CLIENT_SECRET)
                    .refresh_token(request.getRefreshToken())
                    .build());
        } catch (FeignException.BadRequest e) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        } catch (FeignException e) {
            throw new AppException(ErrorCode.FEIGN_ERROR);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }


    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshRequest request) {

        try {
            return keyCloakClient.refreshToken(RefreshTokenExchangeParam.builder()
                    .client_id(CLIENT_ID)
                    .client_secret(CLIENT_SECRET)
                    .scope(SCOPE)
                    .grant_type("refresh_token")
                    .refresh_token(request.getRefreshToken())
                    .build());
        } catch (FeignException.BadRequest e) {
            throw new AppException(ErrorCode.ERROR_REFRESH_TOKEN);
        } catch (FeignException e) {
            throw new AppException(ErrorCode.FEIGN_ERROR);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }


    }
}
