package com.nhattung.chatbotservice.repository;

import com.nhattung.chatbotservice.config.GeminiFeignConfig;
import com.nhattung.chatbotservice.request.GeminiRequest;
import com.nhattung.chatbotservice.response.GeminiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "gemini-client", url = "${gemini.url}", configuration = GeminiFeignConfig.class)
public interface GeminiClient {
    @PostMapping(value = "/models/gemini-1.5-flash:generateContent",
            produces = MediaType.APPLICATION_JSON_VALUE)
    GeminiResponse generateContent(@RequestBody GeminiRequest request);
}
