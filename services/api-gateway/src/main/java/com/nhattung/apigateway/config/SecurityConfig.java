package com.nhattung.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${app.apiPrefix}")
    private String apiPrefix;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        String[] PUBLIC_ENDPOINTS = {
                apiPrefix + "/registration",
                apiPrefix + "/auth/**",
                apiPrefix + "/notification/**",
                apiPrefix + "/otp/**",
                apiPrefix +  "/products/product/**",
                apiPrefix +  "/products/all",
                apiPrefix +  "/products/category/**",
                apiPrefix +  "/products/brand/**",
                apiPrefix +  "/products/name/**",
                apiPrefix +  "/categories/category/**",
                apiPrefix +  "/categories/name/**",
                apiPrefix +  "/categories/all",
                apiPrefix + "/cart/initialize/**",
                apiPrefix + "/user-profile/user/forgot-password",
        };


        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(PUBLIC_ENDPOINTS).permitAll()  // Không cần xác thực
                        .anyExchange()
                        .authenticated()
                )
                .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                        .jwt(Customizer.withDefaults())); // Xác thực bằng JWT
        return http.build();
    }
}
