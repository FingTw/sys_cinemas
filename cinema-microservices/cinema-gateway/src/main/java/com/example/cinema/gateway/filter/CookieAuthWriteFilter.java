package com.example.cinema.gateway.filter;

import com.example.cinema.gateway.config.CookieProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Filter ghi Cookie — chạy SAU khi nhận response từ downstream (IAM).
 *
 * Xử lý 3 endpoint:
 *  1. POST /api/v1/auth/login    → Bóc token ra khỏi body, gắn vào Set-Cookie HttpOnly
 *  2. POST /api/v1/auth/refresh-token → Tương tự login
 *  3. POST /api/v1/auth/logout   → Xóa Cookie (maxAge=0)
 *
 * Body trả về cho Frontend sẽ KHÔNG còn chứa token nữa,
 * chỉ giữ lại thông tin user (username, roles, userId).
 */
@Component
public class CookieAuthWriteFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CookieAuthWriteFilter.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final CookieProperties cookieProperties;
    private final ObjectMapper objectMapper;

    public CookieAuthWriteFilter(CookieProperties cookieProperties, ObjectMapper objectMapper) {
        this.cookieProperties = cookieProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        log.info("[COOKIE-WRITE-FILTER] Received {} {}", method, path);

        // Đăng ký beforeCommit để tự động xóa cookie nếu response trả về 401 Unauthorized
        exchange.getResponse().beforeCommit(() -> Mono.fromRunnable(() -> {
            try {
                HttpStatus statusCode = HttpStatus.resolve(exchange.getResponse().getStatusCode().value());
                if (statusCode == HttpStatus.UNAUTHORIZED) {
                    log.warn("[COOKIE-WRITE] Response status is 401 Unauthorized for path {}. Clearing cookies.", path);
                    clearAllCookies(exchange.getResponse());
                }
            } catch (Exception e) {
                log.error("[COOKIE-WRITE] Error checking/clearing cookies on 401: {}", e.getMessage());
            }
        }));

        // Chỉ chặn POST tới các auth endpoint
        if (!"POST".equalsIgnoreCase(method)) {
            return chain.filter(exchange);
        }

        if (path.equals("/api/v1/auth/logout")) {
            return handleLogout(exchange, chain);
        }

        if (path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/refresh-token") || path.equals("/api/v1/auth/sso/token")) {
            return handleLoginOrRefresh(exchange, chain);
        }

        return chain.filter(exchange);
    }

    /**
     * Xử lý Login & Refresh Token:
     * - Đọc response body từ IAM (chứa token, refreshToken)
     * - Gắn vào HttpOnly Cookie
     * - Xóa token khỏi body, chỉ giữ thông tin user
     * - Tạo CSRF token
     */
    private Mono<Void> handleLoginOrRefresh(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                // Chỉ xử lý nếu response thành công (2xx)
                HttpStatus statusCode = HttpStatus.resolve(getStatusCode().value());
                if (statusCode == null || !statusCode.is2xxSuccessful()) {
                    return super.writeWith(body);
                }

                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(
                        DataBufferUtils.join(fluxBody).map(dataBuffer -> {
                            byte[] content = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(content);
                            DataBufferUtils.release(dataBuffer);
                            return processAuthResponse(content, originalResponse, bufferFactory);
                        })
                    );
                }

                // Mono body
                return DataBufferUtils.join(Flux.from(body)).flatMap(dataBuffer -> {
                    byte[] content = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(content);
                    DataBufferUtils.release(dataBuffer);
                    DataBuffer newBuffer = processAuthResponse(content, originalResponse, bufferFactory);
                    return super.writeWith(Mono.just(newBuffer));
                });
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    /**
     * Xử lý nội dung response từ IAM:
     * 1. Parse JSON → lấy token, refreshToken
     * 2. Gắn vào HttpOnly Cookie
     * 3. Tạo CSRF Cookie
     * 4. Xóa token khỏi body
     */
    private DataBuffer processAuthResponse(byte[] content, ServerHttpResponse response, DataBufferFactory bufferFactory) {
        try {
            String bodyString = new String(content, StandardCharsets.UTF_8);
            Map<String, Object> bodyMap = objectMapper.readValue(bodyString, MAP_TYPE);

            String accessToken = (String) bodyMap.get("token");
            String refreshToken = (String) bodyMap.get("refreshToken");

            if (accessToken != null && !accessToken.isEmpty()) {
                // 1. Set Access Token Cookie (HttpOnly, Secure, SameSite)
                ResponseCookie accessCookie = ResponseCookie
                        .from(cookieProperties.getAccessTokenName(), accessToken)
                        .httpOnly(true)
                        .secure(cookieProperties.isSecure())
                        .sameSite(cookieProperties.getSameSite())
                        .path("/")
                        .maxAge(Duration.ofSeconds(cookieProperties.getAccessTokenMaxAge()))
                        .build();
                response.addCookie(accessCookie);

                log.info("[COOKIE-WRITE] Set HttpOnly ACCESS_TOKEN cookie");
            }

            if (refreshToken != null && !refreshToken.isEmpty()) {
                // 2. Set Refresh Token Cookie (HttpOnly, path giới hạn)
                ResponseCookie refreshCookie = ResponseCookie
                        .from(cookieProperties.getRefreshTokenName(), refreshToken)
                        .httpOnly(true)
                        .secure(cookieProperties.isSecure())
                        .sameSite(cookieProperties.getSameSite())
                        .path("/api/v1/auth/refresh-token")
                        .maxAge(Duration.ofSeconds(cookieProperties.getRefreshTokenMaxAge()))
                        .build();
                response.addCookie(refreshCookie);
            }

            // 3. Set CSRF Token Cookie (KHÔNG HttpOnly — JS cần đọc được)
            String csrfToken = UUID.randomUUID().toString();
            ResponseCookie csrfCookie = ResponseCookie
                    .from(cookieProperties.getCsrfTokenName(), csrfToken)
                    .httpOnly(false) // JS cần đọc được để gửi lại qua Header
                    .secure(cookieProperties.isSecure())
                    .sameSite(cookieProperties.getSameSite())
                    .path("/")
                    .maxAge(Duration.ofSeconds(cookieProperties.getAccessTokenMaxAge()))
                    .build();
            response.addCookie(csrfCookie);

            // 4. Xóa token khỏi body trả về Frontend (giữ lại user info)
            bodyMap.remove("token");
            bodyMap.remove("refreshToken");

            byte[] newContent = objectMapper.writeValueAsBytes(bodyMap);
            response.getHeaders().setContentLength(newContent.length);
            return bufferFactory.wrap(newContent);

        } catch (Exception e) {
            log.error("[COOKIE-WRITE] Failed to process auth response: {}", e.getMessage());
            // Trả về nội dung gốc nếu parse lỗi
            return bufferFactory.wrap(content);
        }
    }

    /**
     * Xử lý cài đặt xóa toàn bộ Cookie xác thực trên Response.
     */
    private void clearAllCookies(ServerHttpResponse response) {
        // Xóa Access Token Cookie
        response.addCookie(ResponseCookie
                .from(cookieProperties.getAccessTokenName(), "")
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path("/")
                .maxAge(0)
                .build());

        // Xóa Refresh Token Cookie
        response.addCookie(ResponseCookie
                .from(cookieProperties.getRefreshTokenName(), "")
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path("/api/v1/auth/refresh-token")
                .maxAge(0)
                .build());

        // Xóa CSRF Cookie
        response.addCookie(ResponseCookie
                .from(cookieProperties.getCsrfTokenName(), "")
                .httpOnly(false)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path("/")
                .maxAge(0)
                .build());
    }

    /**
     * Xử lý Logout: Xóa tất cả Cookie (maxAge=0)
     */
    private Mono<Void> handleLogout(ServerWebExchange exchange, GatewayFilterChain chain) {
        clearAllCookies(exchange.getResponse());
        log.info("[COOKIE-WRITE] Configured clearance of all auth cookies on logout response");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Chạy sớm để có thể decorate response trước khi gửi về client
        return -100;
    }
}
