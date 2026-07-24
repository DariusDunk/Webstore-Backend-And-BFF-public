package com.example.ecomerseapplication.Auth.config;

import org.springframework.beans.factory.annotation.Value;
import  org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.client.provider.keycloak.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtIssuerValidator(issuerUri)
        );

        jwtDecoder.setJwtValidator(validator);

        return jwtDecoder;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
//                                request -> endpointMatcher.isPublicOrSemiProtected(request.getRequestURI())
//----------------------------------public endpoints----------------------------------
                                "/attributes/**",
                                "/category/**",
                                "/manufacturer/**",
                                "/product/*",
                                "/product/*/review/overview",
                                "/product/manufacturer/*/p*",//todo opravi strukturata na URL-a da dyrji "p" i samata stranica otdeleni
                                "/product/category/*/p*",//todo opravi strukturata na URL-a da dyrji "p" i samata stranica otdeleni
                                "/product/filter/*",
                                "/product/codes/stockValidation",
                                "/emails/*",
                                "/error",
//----------------------------------public endpoints----------------------------------
//----------------------------------semi-protected endpoints----------------------------------
                                "/auth/**",
                                "/cart/*",
                                "/cart/remove/*",
                                "/cart/add/quantity",
                                "/product/detail/*",
                                "/product/reviews/paged",
                                "/purchase/*"
//----------------------------------semi-protected endpoints----------------------------------
                        ).permitAll() // public endpoints
//                                .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            if (realmAccess != null && realmAccess.containsKey("roles")) {

                Collection<String> roles = safeStringCollection(realmAccess.get("roles"));

                authorities.addAll(
                        roles.stream()
                                .map(role -> "ROLE_" + role.toUpperCase())
                                .map(SimpleGrantedAuthority::new)
                                .toList()
                );

            }

            return authorities;
        });

        return converter;
    }

    private Collection<String> safeStringCollection(Object obj) {
        if (obj instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return List.of();
    }

}
