package com.nhattung.orderservice.repository.httpclient;

import com.nhattung.orderservice.request.HandleUserPromotionRequest;
import com.nhattung.orderservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "promotion-service",                 // vẫn trùng Eureka
        contextId = "promotionPublicClient"         // khác contextId để tránh xung đột
)
public interface PromotionPublicClient {
    @PutMapping(value = "/promotions/update-user-promotion", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Void> updateUserPromotion(@RequestBody HandleUserPromotionRequest request);
}