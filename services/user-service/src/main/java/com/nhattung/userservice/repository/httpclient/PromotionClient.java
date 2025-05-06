package com.nhattung.userservice.repository.httpclient;

import com.nhattung.userservice.config.AuthRequestInterceptor;
import com.nhattung.userservice.request.HandleUserPromotionRequest;
import com.nhattung.userservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "promotion-service")
public interface PromotionClient {

    @PostMapping(value = "/promotions/create-user-promotion", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Void> createUserPromotion(@RequestBody HandleUserPromotionRequest request);
}
