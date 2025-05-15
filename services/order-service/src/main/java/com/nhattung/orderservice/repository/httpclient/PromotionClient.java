package com.nhattung.orderservice.repository.httpclient;

import com.nhattung.orderservice.config.AuthRequestInterceptor;
import com.nhattung.orderservice.dto.PromotionDto;
import com.nhattung.orderservice.request.HandleUserPromotionRequest;
import com.nhattung.orderservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "promotion-service",                  // trùng với Eureka
        contextId = "promotionSecuredClient",           // tên duy nhất
        configuration = AuthRequestInterceptor.class) // có token
public interface PromotionClient {

    @GetMapping(value = "/promotions/active/{promotionCode}",produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<PromotionDto> getActivePromotionByCode(@PathVariable("promotionCode") String promotionCode);
}
