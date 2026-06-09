package com.example.cinema.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class ClientSecurityGuardFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(ClientSecurityGuardFilter.class);

    private final DynamicSecurityConfiguration dynamicSecurityConfiguration;

    public ClientSecurityGuardFilter(DynamicSecurityConfiguration dynamicSecurityConfiguration) {
        this.dynamicSecurityConfiguration = dynamicSecurityConfiguration;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Get dynamic configuration values
        String expectedClientKey = dynamicSecurityConfiguration.getExpectedClientKey();
        List<String> protectedPaths = dynamicSecurityConfiguration.getProtectedPaths();

        // Check if the request target is one of the public endpoints
        boolean isPublicPath = protectedPaths.stream().anyMatch(path::startsWith);

        if (isPublicPath) {
            String clientKey = request.getHeaders().getFirst("X-Client-Key");
            if (clientKey == null) {
                clientKey = request.getHeaders().getFirst("X-API-Key");
            }
            if (clientKey == null || !clientKey.equals(expectedClientKey)) {
                log.warn("Blocked direct access to public endpoint [{}] from IP: {}. Missing or invalid X-Client-Key/X-API-Key.",
                        path, request.getRemoteAddress());
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return response.setComplete();
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -200; // Run early
    }
}
