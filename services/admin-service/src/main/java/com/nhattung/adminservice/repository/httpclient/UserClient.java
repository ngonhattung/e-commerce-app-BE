package com.nhattung.adminservice.repository.httpclient;


import com.nhattung.adminservice.config.AuthRequestInterceptor;
import com.nhattung.adminservice.dto.MonthlyRegistrationDto;
import com.nhattung.adminservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "user-service", configuration = AuthRequestInterceptor.class)
public interface UserClient {


    @GetMapping(value = "/user-profile/count", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Long> getTotalUserCount();


    @GetMapping(value = "/user-profile/monthly-registrations", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<MonthlyRegistrationDto>> getMonthlyRegistrationData();
}
