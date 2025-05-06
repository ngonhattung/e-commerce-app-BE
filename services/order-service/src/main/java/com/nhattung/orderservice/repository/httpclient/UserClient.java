package com.nhattung.orderservice.repository.httpclient;

import com.nhattung.orderservice.config.AuthRequestInterceptor;
import com.nhattung.orderservice.dto.UserProfileDto;
import com.nhattung.orderservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", configuration = AuthRequestInterceptor.class)
public interface UserClient {


    @GetMapping(value = "/user-profile/user/profile/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserProfileDto> getUserProfileById(@PathVariable("userId") String userId);
}
