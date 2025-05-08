package com.nhattung.authservice.repository;


import com.nhattung.authservice.dto.UserProfileDto;
import com.nhattung.authservice.request.CreateUserProfileGGFBRequest;
import com.nhattung.authservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface UserClient {
    @PostMapping(value = "/user-profile/create", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserProfileDto> createUserProfile(@RequestBody CreateUserProfileGGFBRequest request);
}
