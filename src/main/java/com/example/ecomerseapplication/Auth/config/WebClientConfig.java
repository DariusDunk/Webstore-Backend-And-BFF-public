package com.example.ecomerseapplication.Auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${keycloak.server-url}")
    private String tokenAddress;

    public WebClientConfig() {
    }

    @Bean
    public WebClient keycloakWebClient() {
        return WebClient.builder()
                .baseUrl(tokenAddress)
                .build();
    }

}
