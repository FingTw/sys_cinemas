package com.example.cinema.gateway.filter;

import com.example.cinema.gateway.config.CookieProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;

@Component
public class CookieAuthReadFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CookieAuthReadFilter.class);

    private final CookieProperties cookieProperties;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CookieAuthReadFilter(CookieProperties cookieProperties, ReactiveStringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.cookieProperties = cookieProperties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        if (path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/callback") || path.startsWith("/api/v1/auth/register")) {
            return chain.filter(exchange);
        }

        // /me tự tạo Redis cache → không thể đọc cache để tự authenticate
        // Chỉ forward JWT + userId, bỏ qua Redis lookup
        if (path.startsWith("/api/v1/auth/me")) {
            String jwt = extractJwt(request);
            if (jwt != null) {
                String userId = extractSubjectFromJwt(jwt);
                ServerHttpRequest.Builder builder = request.mutate()
                    .header("Authorization", "Bearer " + jwt);
                if (userId != null) {
                    builder.header("X-User-Id", userId);
                }
                return chain.filter(exchange.mutate().request(builder.build()).build());
            }
            return chain.filter(exchange);
        }

        String jwt = extractJwt(request);

        if (jwt != null && !jwt.isEmpty()) {
            final String finalJwt = jwt;
            String userId = extractSubjectFromJwt(jwt);
            
            if (userId != null) {
                return redisTemplate.opsForValue().get("user_perms:" + userId)
                    .flatMap(redisData -> {
                        try {
                            JsonNode json = objectMapper.readTree(redisData);
                            String roles = json.has("roles") ? json.get("roles").asText() : "";
                            String permissions = json.has("permissions") ? json.get("permissions").asText() : "";

                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                    .header("Authorization", "Bearer " + finalJwt)
                                    .header("X-User-Roles", roles)
                                    .header("X-User-Permissions", permissions)
                                    .header("X-User-Id", userId)
                                    .build();
                                    
                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        } catch (Exception e) {
                            log.error("Failed to parse permissions from Redis", e);
                            ServerHttpRequest fallbackReq = exchange.getRequest().mutate()
                                .header("Authorization", "Bearer " + finalJwt)
                                .header("X-User-Id", userId)
                                .build();
                            return chain.filter(exchange.mutate().request(fallbackReq).build());
                        }
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        ServerHttpRequest fallbackReq = exchange.getRequest().mutate()
                            .header("Authorization", "Bearer " + finalJwt)
                            .header("X-User-Id", userId)
                            .build();
                        return chain.filter(exchange.mutate().request(fallbackReq).build());
                    }));
            } else {
                ServerHttpRequest fallbackReq = exchange.getRequest().mutate().header("Authorization", "Bearer " + finalJwt).build();
                return chain.filter(exchange.mutate().request(fallbackReq).build());
            }
        }

        return chain.filter(exchange);
    }

    private String extractJwt(ServerHttpRequest request) {
        String existingAuth = request.getHeaders().getFirst("Authorization");
        if (existingAuth != null && existingAuth.startsWith("Bearer ")) {
            return existingAuth.substring(7);
        }
        HttpCookie accessTokenCookie = request.getCookies().getFirst(cookieProperties.getAccessTokenName());
        if (accessTokenCookie != null && !accessTokenCookie.getValue().isEmpty()) {
            return accessTokenCookie.getValue();
        }
        return null;
    }

    private String extractSubjectFromJwt(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length == 3) {
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                JsonNode jsonNode = objectMapper.readTree(payload);
                if (jsonNode.has("sub")) {
                    return jsonNode.get("sub").asText();
                }
            }
        } catch (Exception e) {
            log.error("Failed to decode JWT to extract subject", e);
        }
        return null;
    }

    @Override
    public int getOrder() {
        return -150;
    }
}
