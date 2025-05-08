package com.nhattung.authservice.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhattung.authservice.exception.AppException;
import com.nhattung.authservice.exception.ErrorCode;
import com.nhattung.authservice.repository.KeyCloakClient;
import com.nhattung.authservice.repository.UserClient;
import com.nhattung.authservice.request.*;
import com.nhattung.authservice.response.AuthResponse;
import com.nhattung.authservice.response.LogoutResponse;
import com.nhattung.authservice.response.RefreshTokenResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService{

    private final KeyCloakClient keyCloakClient;
    private final UserClient userClient;
    @Value("${idp.client.id}")
    private String CLIENT_ID;

    @Value("${idp.client.secret}")
    private String CLIENT_SECRET;

    @Value("${idp.client.scope}")
    private String SCOPE;

    @Value("${idp.client.grant-type}")
    private String GRANT_TYPE;

    @Value("${idp.url}")
    private String IDP_URL;

    @Value("${idp.frontendUrl}")
    private String FRONTEND_URL;


    @Override
    public AuthResponse exchangeToken(LoginRequest request) {
        try {
            // Get the token response from Keycloak
            AuthResponse response = keyCloakClient.exchangeToken(TokenExchangeParam.builder()
                    .client_id(CLIENT_ID)
                    .client_secret(CLIENT_SECRET)
                    .username(request.getUsername().trim())
                    .password(request.getPassword())
                    .grant_type(GRANT_TYPE)
                    .scope(SCOPE)
                    .build());

            // Extract roles from the access token
            List<String> roles = extractRolesFromToken(response.getAccessToken());
            response.setRoles(roles);

            return response;
        } catch (FeignException.Unauthorized e) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        } catch (FeignException e) {
            throw new AppException(ErrorCode.FEIGN_ERROR);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

    }

    private List<String> extractRolesFromToken(String accessToken) {
        try {
            // Split the token
            String[] chunks = accessToken.split("\\.");

            // Get the payload part (second chunk)
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));

            // Parse the payload as JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode payloadJson = mapper.readTree(payload);

            // Extract roles from the token
            // The path to roles might vary depending on your Keycloak configuration
            // Common paths include: realm_access.roles, resource_access.[client-id].roles
            List<String> roles = new ArrayList<>();

            // Check for realm roles
            if (payloadJson.has("realm_access") && payloadJson.get("realm_access").has("roles")) {
                JsonNode rolesNode = payloadJson.get("realm_access").get("roles");
                if (rolesNode.isArray()) {
                    for (JsonNode role : rolesNode) {
                        roles.add(role.asText());
                    }
                }
            }

            return roles;
        } catch (Exception e) {
            // Handle exceptions or return empty list
            return new ArrayList<>();
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

    @Override
    public String createGoogleAuthUrl() {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(IDP_URL+"/auth")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri",FRONTEND_URL + "/callback")
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("kc_idp_hint", "google");

        return builder.toUriString();
    }

    @Override
    public String createFacebookAuthUrl() {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(IDP_URL+"/auth")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("redirect_uri",FRONTEND_URL + "/callback")
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("kc_idp_hint", "facebook");

        return builder.toUriString();
    }

    @Override
    public AuthResponse getTokenFromCode(String code) {
        try {
            AuthResponse response =  keyCloakClient.getTokenFromCode(TokenFromCodeParam.builder()
                    .client_id(CLIENT_ID)
                    .client_secret(CLIENT_SECRET)
                    .grant_type("authorization_code")
                    .code(code)
                    .redirect_uri(FRONTEND_URL + "/callback")
                    .build());

            // Extract roles from the access token
            List<String> roles = extractRolesFromToken(response.getAccessToken());
            response.setRoles(roles);

            Map<String, Object> userInfo = keyCloakClient.getUserInfo("Bearer " + response.getAccessToken());
            String userId = (String) userInfo.get("sub");
            String email = (String) userInfo.get("email");
            String fullName = (String) userInfo.get("name");
            String avatar = "https://ui-avatars.com/api/?name=" + fullName.replace(" ", "+") + "&background=random&color=fff&bold=true&size=128";
            CreateUserProfileGGFBRequest createUserProfileGGFBRequest = CreateUserProfileGGFBRequest.builder()
                    .userId(userId)
                    .email(email)
                    .fullName(fullName)
                    .avatar(avatar)
                    .build();
            userClient.createUserProfile(createUserProfileGGFBRequest);

            return response;
        } catch (FeignException.BadRequest e) {
            throw new AppException(ErrorCode.ERROR_GET_TOKEN_FROM_CODE);
        } catch (FeignException e) {
            throw new AppException(ErrorCode.FEIGN_ERROR);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }


}
