package com.nhattung.authservice.repository;


import com.nhattung.authservice.request.LogoutParam;
import com.nhattung.authservice.request.RefreshTokenParam;
import com.nhattung.authservice.request.TokenExchangeParam;
import com.nhattung.authservice.response.AuthResponse;
import com.nhattung.authservice.response.LogoutResponse;
import com.nhattung.authservice.response.RefreshTokenResponse;
import feign.Param;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "keycloak-service", url = "${idp.url}")
public interface KeyCloakClient {

    @PostMapping(value = "/token",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    AuthResponse exchangeToken(@RequestBody TokenExchangeParam param);

    @PostMapping(value = "/logout",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    LogoutResponse logout(@QueryMap LogoutParam param);

    @PostMapping(value = "/refresh",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    RefreshTokenResponse refreshToken(@QueryMap RefreshTokenParam param);
}
