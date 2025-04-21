package com.nhattung.chatbotservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiFeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(@Value("${gemini.api-key}") String apiKey) {
        return template -> template.query("key", apiKey);
    }
}
