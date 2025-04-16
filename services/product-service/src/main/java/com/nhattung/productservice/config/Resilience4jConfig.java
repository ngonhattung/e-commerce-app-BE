package com.nhattung.productservice.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Resilience4jConfig {


    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {

        //Call 5 times per minute
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(5)
                .limitRefreshPeriod(java.time.Duration.ofMinutes(1))
                .timeoutDuration(java.time.Duration.ofSeconds(1))
                .build();
        return RateLimiterRegistry.of(config);
    }

    @Bean
    public RateLimiter inventoryServiceRateLimiter(RateLimiterRegistry rateLimiterRegistry) {
        return rateLimiterRegistry.rateLimiter("inventoryService");
    }
}
