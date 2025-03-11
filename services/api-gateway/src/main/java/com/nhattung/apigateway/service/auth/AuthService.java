package com.nhattung.apigateway.service.auth;

import com.nhattung.apigateway.repository.httpclient.AuthClient;
import com.nhattung.apigateway.request.IntrospectRequest;
import com.nhattung.apigateway.response.ApiResponse;
import com.nhattung.apigateway.response.IntrospectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthClient authClient;

    public  Mono<ApiResponse<IntrospectResponse>> introspect(String token) {
        return authClient.introspect(IntrospectRequest.builder()
                .token(token)
                .build());
    }
}
