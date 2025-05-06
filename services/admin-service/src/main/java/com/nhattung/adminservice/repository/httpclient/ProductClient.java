package com.nhattung.adminservice.repository.httpclient;

import com.nhattung.adminservice.config.AuthRequestInterceptor;
import com.nhattung.adminservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "product-service", configuration = AuthRequestInterceptor.class)
public interface ProductClient {


    @GetMapping(value = "/products/count",produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Long> getTotalProductCount();
}
