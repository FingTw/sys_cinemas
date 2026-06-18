package com.example.cinema.gateway.filter;

import com.example.cinema.gateway.config.CookieProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * CSRF Protection Filter — Double Submit Cookie Pattern.
 *
 * Khi chuyển sang Cookie-based auth, cần chống CSRF vì trình duyệt
 * tự động gửi Cookie trong mọi request (kể cả từ trang web khác).
 *
 * Cơ chế:
 *  1. Khi login, CookieAuthWriteFilter đã tạo Cookie XSRF-TOKEN (KHÔNG HttpOnly)
 *  2. Frontend đọc Cookie này và gắn vào Header X-XSRF-TOKEN mỗi request
 *  3. Filter này so sánh: Cookie XSRF-TOKEN == Header X-XSRF-TOKEN
 *     → Nếu khớp: request hợp lệ (chỉ trang web gốc mới đọc được Cookie để gửi Header)
 *     → Nếu không khớp: request giả mạo (trang web khác chỉ gửi được Cookie, không đọc được)
 *
 * Bỏ qua kiểm tra:
 *  - GET, HEAD, OPTIONS (safe methods)
 *  - Login, Register, Public Key, Password Policy (chưa có CSRF token)
 *  - Các request không có Cookie (chưa đăng nhập)
 */
@Component
public class CsrfTokenFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CsrfTokenFilter.class);

    private final CookieProperties cookieProperties;

    // Các path được miễn kiểm tra CSRF (public endpoints, login/register)
    private static final Set<String> CSRF_EXEMPT_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/public-key",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/password-policy",
            "/api/v1/auth/check-username",
            "/api/v1/auth/check-email",
            "/api/v1/vnpay"
    );

    // Safe HTTP methods — không cần CSRF
    private static final Set<HttpMethod> SAFE_METHODS = Set.of(
            HttpMethod.GET,
            HttpMethod.HEAD,
            HttpMethod.OPTIONS
    );

    public CsrfTokenFilter(CookieProperties cookieProperties) {
        this.cookieProperties = cookieProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();
        String path = request.getURI().getPath();

        // 1. Skip safe methods (GET, HEAD, OPTIONS)
        if (method != null && SAFE_METHODS.contains(method)) {
            return chain.filter(exchange);
        }

        // 2. Skip exempt paths
        boolean isExempt = CSRF_EXEMPT_PATHS.stream().anyMatch(path::startsWith);
        if (isExempt) {
            return chain.filter(exchange);
        }

        // 3. Skip nếu không có Cookie (chưa đăng nhập, hoặc sử dụng Bearer Token trực tiếp)
        HttpCookie accessTokenCookie = request.getCookies().getFirst(cookieProperties.getAccessTokenName());
        if (accessTokenCookie == null) {
            return chain.filter(exchange);
        }

        // 4. Kiểm tra CSRF: So sánh Cookie vs Header
        HttpCookie csrfCookie = request.getCookies().getFirst(cookieProperties.getCsrfTokenName());
        String csrfHeader = request.getHeaders().getFirst("X-XSRF-TOKEN");

        if (csrfCookie == null || csrfHeader == null || !csrfCookie.getValue().equals(csrfHeader)) {
            log.warn("[CSRF] Blocked request to {} — CSRF token mismatch or missing. IP: {}",
                    path, request.getRemoteAddress());

            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Chạy SAU CookieAuthReadFilter (-150), TRƯỚC khi forward downstream
        return -140;
    }
}
