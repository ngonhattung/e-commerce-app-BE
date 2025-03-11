package com.nhattung.authservice.service.auth;

import com.nhattung.authservice.request.IntrospectRequest;
import com.nhattung.authservice.response.IntrospectResponse;
import com.nhattung.authservice.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IntrospectService {
    private final JwtUtils jwtUtils;

    public IntrospectResponse introspect(IntrospectRequest request) {
        String token = request.getToken();
        boolean isValid = true;
        if (!jwtUtils.validateJwtToken(token)) {
            isValid = false;
        }
        return IntrospectResponse
                .builder()
                .valid(isValid)
                .build();
    }
}
