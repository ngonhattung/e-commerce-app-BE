package com.nhattung.cartservice.repository.httpclient;

import com.nhattung.cartservice.dto.ProductDto;
import com.nhattung.cartservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping(value = "/products/product/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<ProductDto> getProductById(@PathVariable Long id);
}
