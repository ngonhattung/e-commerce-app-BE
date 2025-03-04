package com.nhattung.authservice.repository.httpclient;

import com.nhattung.authservice.request.CreateUserProfileRequest;
import com.nhattung.authservice.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service",
        url = "http://localhost:8091/api/v1/user-profile",
        fallback = UserClientImpl.class)
public interface UserClient {

    @PostMapping(value = "/add",produces = MediaType.APPLICATION_JSON_VALUE)
    UserProfileResponse createUserProfile(@RequestBody CreateUserProfileRequest request);

}
