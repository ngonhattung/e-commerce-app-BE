package com.nhattung.orderservice.repository.httpclient;

import com.nhattung.orderservice.config.AuthRequestInterceptor;
import com.nhattung.orderservice.dto.PromotionDto;
import com.nhattung.orderservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "promotion-service", configuration = {AuthRequestInterceptor.class})
public interface PromotionClient {

    @GetMapping(value = "/active/{promotionCode}",produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<PromotionDto> getActivePromotionByCode(@PathVariable("promotionCode") String promotionCode);
}
