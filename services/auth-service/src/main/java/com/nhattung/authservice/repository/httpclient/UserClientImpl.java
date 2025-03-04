package com.nhattung.authservice.repository.httpclient;

import com.nhattung.authservice.request.CreateUserProfileRequest;
import com.nhattung.authservice.response.UserProfileResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
@Slf4j
public class UserClientImpl implements UserClient {
    @Override
    public UserProfileResponse createUserProfile(CreateUserProfileRequest request) {
       log.info("Register no success");
        return null;
    }
}
