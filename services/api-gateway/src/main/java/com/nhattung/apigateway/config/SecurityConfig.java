package com.nhattung.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${app.apiPrefix}")
    private String apiPrefix;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        String[] PUBLIC_ENDPOINTS = {
                apiPrefix + "/user-profile/registration",
                apiPrefix + "/auth/**",
                apiPrefix + "/notification/**"
        };

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(PUBLIC_ENDPOINTS).permitAll()  // Không cần xác thực
                        .anyExchange()
                        .authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())); // Xác thực bằng JWT
        return http.build();
    }
}
