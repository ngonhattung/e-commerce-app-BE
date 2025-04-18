package com.nhattung.orderservice.repository.httpclient;

import com.nhattung.orderservice.config.AuthRequestInterceptor;
import com.nhattung.orderservice.dto.ProductDto;
import com.nhattung.orderservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "product-service",
        configuration = {AuthRequestInterceptor.class})
public interface ProductClient {

    @GetMapping(value = "/products/productsByIds",produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<ProductDto>> getProductsByIds(@RequestParam List<Long> ids);
}
