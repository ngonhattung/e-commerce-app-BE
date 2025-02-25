package com.nhattung.authservice.request;

import lombok.Data;

@Data
public class RefreshRequest {
    private String token;
}
