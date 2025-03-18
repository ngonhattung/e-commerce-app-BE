package com.nhattung.userservice.config;


import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Value("${idp.url}")
    private String SERVER_URL;
    @Value("${idp.client.realm}")
    private String REALM;
    @Value("${idp.client.id}")
    private String CLIENT_ID;
    @Value("${idp.client.secret}")
    private String CLIENT_SECRET;
    @Value("${idp.client.grant-type}")
    private String GRANT_TYPE;
    @Value("${idp.client.scope}")
    private String SCOPE;
    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(SERVER_URL)
                .realm(REALM)
                .clientId(CLIENT_ID)
                .grantType(GRANT_TYPE)
                .clientSecret(CLIENT_SECRET)
                .scope(SCOPE)
                .build();
    }
}
