package com.nhattung.userservice.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "cart-service")
public interface CartClient {

    @PostMapping(value = "/cart/initialize/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    void initializeCart(@PathVariable String userId);
}
