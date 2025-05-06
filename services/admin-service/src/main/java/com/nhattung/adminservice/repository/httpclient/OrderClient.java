package com.nhattung.adminservice.repository.httpclient;

import com.nhattung.adminservice.config.AuthRequestInterceptor;
import com.nhattung.adminservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

@FeignClient(name = "order-service", configuration = AuthRequestInterceptor.class)
public interface OrderClient {


    @GetMapping(value = "/orders/count", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Long> getTotalOrderCount();

    @GetMapping(value = "/orders/revenue", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<BigDecimal> getTotalOrderRevenue();
}
