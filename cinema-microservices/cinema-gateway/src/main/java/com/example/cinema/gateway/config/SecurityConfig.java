package com.example.cinema.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // Creates a decoder that validates the signature using the JWK Set URI.
        // It does NOT validate the issuer, avoiding the localhost vs cinema-keycloak mismatch.
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .headers(headers -> headers.frameOptions(org.springframework.security.config.web.server.ServerHttpSecurity.HeaderSpec.FrameOptionsSpec::disable))
            .authorizeExchange(exchanges -> exchanges
                // Permit all requests at the gateway level.
                // Downstream services will enforce authorization.
                // This just enables JWT signature validation if a JWT is present.
                .anyExchange().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})
                .bearerTokenConverter(exchange -> {
                    String path = exchange.getRequest().getURI().getPath();
                    if (path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/callback") || path.startsWith("/api/v1/auth/register")) {
                        return reactor.core.publisher.Mono.empty();
                    }
                    String token = null;
                    // First check Authorization header
                    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        token = authHeader.substring(7);
                    } else {
                        // Then check cookie
                        org.springframework.http.HttpCookie cookie = exchange.getRequest().getCookies().getFirst("ACCESS_TOKEN");
                        if (cookie != null) {
                            token = cookie.getValue();
                        }
                    }
                    if (token != null && !token.isEmpty()) {
                        return reactor.core.publisher.Mono.just(new org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken(token));
                    }
                    return reactor.core.publisher.Mono.empty();
                })
            );
            
        return http.build();
    }
}
