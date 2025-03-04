package com.nhattung.authservice.repository.httpclient;

import com.nhattung.authservice.request.CreateUserProfileRequest;
import com.nhattung.authservice.response.UserProfileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
public class UserClientImpl implements UserClient {
    @Override
    public UserProfileResponse createUserProfile(CreateUserProfileRequest request) {
       log.error("Register is loading");
        return null;
    }
}
