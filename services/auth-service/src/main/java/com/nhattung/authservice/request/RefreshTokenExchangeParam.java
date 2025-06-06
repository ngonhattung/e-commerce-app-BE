package com.nhattung.authservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenExchangeParam {
    private String grant_type;
    private String client_id;
    private String client_secret;
    private String scope;
    private String refresh_token;
}
