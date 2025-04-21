package com.nhattung.chatbotservice.repository;

import com.nhattung.chatbotservice.request.GeminiRequest;
import com.nhattung.chatbotservice.response.GeminiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gemini-client", url = "${gemini.url}")
public interface GeminiClient {
    @PostMapping("/models/gemini-pro:generateContent")
    GeminiResponse generateContent(@RequestHeader("Authorization") String apiKey,
                                   @RequestBody GeminiRequest request);
}
