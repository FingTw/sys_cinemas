package com.example.cinema.gateway.filter;

import com.example.cinema.gateway.config.CookieProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filter đọc Cookie — chạy TRƯỚC khi forward request xuống downstream.
 *
 * Luồng:
 *  1. Kiểm tra Cookie ACCESS_TOKEN trên request đến
 *  2. Nếu có → trích xuất JWT → inject vào Header "Authorization: Bearer <jwt>"
 *  3. Các service phía sau (IAM, Catalog, Booking...) nhận được Header như cũ
 *     → KHÔNG cần sửa gì ở downstream services
 *
 * Nếu request đã có sẵn Authorization Header (ví dụ gọi từ Postman),
 * sẽ ưu tiên Header hơn Cookie để giữ backward compatibility.
 */
@Component
public class CookieAuthReadFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CookieAuthReadFilter.class);

    private final CookieProperties cookieProperties;

    public CookieAuthReadFilter(CookieProperties cookieProperties) {
        this.cookieProperties = cookieProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Nếu đã có Authorization Header (backward compat với Postman/Mobile),
        // không cần đọc Cookie
        String existingAuth = request.getHeaders().getFirst("Authorization");
        if (existingAuth != null && existingAuth.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        // Đọc Cookie ACCESS_TOKEN
        HttpCookie accessTokenCookie = request.getCookies()
                .getFirst(cookieProperties.getAccessTokenName());

        if (accessTokenCookie != null && !accessTokenCookie.getValue().isEmpty()) {
            String jwt = accessTokenCookie.getValue();

            // Inject Authorization Header cho downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("Authorization", "Bearer " + jwt)
                    .build();

            log.debug("[COOKIE-READ] Injected Authorization header from Cookie for path: {}",
                    request.getURI().getPath());

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Chạy SAU RequestTracingFilter (-300) và ClientSecurityGuardFilter (-200),
        // nhưng TRƯỚC khi Gateway forward request xuống downstream
        return -150;
    }
}
