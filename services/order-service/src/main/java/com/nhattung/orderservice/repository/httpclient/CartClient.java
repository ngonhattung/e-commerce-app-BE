package com.nhattung.orderservice.repository.httpclient;

import com.nhattung.orderservice.config.AuthRequestInterceptor;
import com.nhattung.orderservice.dto.CartDto;
import com.nhattung.orderservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "cart-service",
        configuration = {AuthRequestInterceptor.class})
public interface CartClient {
    @GetMapping(value = "/cart/get",produces = MediaType.APPLICATION_JSON_VALUE)
    CartDto getCart();
}
