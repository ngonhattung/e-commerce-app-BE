package com.nhattung.apigateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhattung.apigateway.exception.ErrorCode;
import com.nhattung.apigateway.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> commence(ServerWebExchange exchange,
                               AuthenticationException ex) {

        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        exchange.getResponse().setStatusCode(errorCode.getStatusCode());
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            ApiResponse<?> apiResponse = ApiResponse.builder()
                    .code(errorCode.getCode())
                    .message(errorCode.getMessage())
                    .build();

            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return Mono.error(e);
        }

    }
}
