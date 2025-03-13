package com.nhattung.apigateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhattung.apigateway.response.ApiResponse;
import com.nhattung.apigateway.response.IntrospectResponse;
import com.nhattung.apigateway.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.resource.HttpResource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    private String[] whiteList = new String[]{
            "/auth/login",
            "/auth/register",
            "/notification/send-email"
    };

    @Value("${app.apiPrefix}")
    private String apiPrefix;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Received request path: {}", exchange.getRequest().getURI().getPath());
        if(isWhiteList(exchange.getRequest())){
            log.info("Request path {} is whitelisted.", exchange.getRequest().getURI().getPath());
            return chain.filter(exchange);
        }

        //get token from the request
        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if(CollectionUtils.isEmpty(authHeader)){
            return unauthenticated(exchange.getResponse());
        }

        String token = authHeader.getFirst().replace("Bearer ", "");
        log.info("Token: {}", token);


        return authService.introspect(token)
                .flatMap(introspectResponse -> {
                    IntrospectResponse data = introspectResponse.getData();
                    if (data != null && data.isValid()) {
                        return chain.filter(exchange);
                    }
                    return unauthenticated(exchange.getResponse());
                })
                .onErrorResume(throwable -> unauthenticated(exchange.getResponse()));
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isWhiteList(ServerHttpRequest request){
        return Arrays
                .stream(whiteList)
                .anyMatch(uri -> request.getURI()
                                .getPath()
                                .matches(apiPrefix + uri));
    }

    Mono<Void> unauthenticated(ServerHttpResponse response) {
        ApiResponse<String> apiResponse = new ApiResponse<>("Unauthenticated", null);
        String body = null;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}
