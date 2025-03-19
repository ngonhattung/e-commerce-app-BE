package com.nhattung.authservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenParam {
    private String client_id;
    private String client_secret;
    private String refresh_token;
}
