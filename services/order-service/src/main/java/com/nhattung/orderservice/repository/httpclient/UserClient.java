package com.nhattung.orderservice.repository.httpclient;

import com.nhattung.orderservice.config.AuthRequestInterceptor;
import com.nhattung.orderservice.dto.UserProfileDto;
import com.nhattung.orderservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service", configuration = AuthRequestInterceptor.class)
public interface UserClient {


    @GetMapping(value = "/user-profile/user/profile/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserProfileDto> getUserProfileById(@PathVariable("userId") String userId);

    @GetMapping(value = "/user-profile/user/search", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<String>> findUserIdsBySearchTerm(@RequestParam("searchTerm") String searchTerm);

    @GetMapping(value = "/user-profile/user/search-fullName", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<String>> findUserIdsByFullName(@RequestParam("fullName") String fullName);

    @GetMapping(value = "/user-profile/user/search-email", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<String>> findUserIdsByEmail(@RequestParam("email") String email);

    @GetMapping(value = "/user-profile/user/search-phone", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<String>> findUserIdsByPhone(@RequestParam("phone") String phone);
}
