package com.example.cinema.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter xác thực X-API-KEY cho các kết nối nội bộ (Inter-service communication).
 * Đảm bảo chỉ có Gateway hoặc các service hợp lệ mới có thể gọi trực tiếp.
 */
@Component
public class XApiKeyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(XApiKeyFilter.class);
    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    @Value("${app.security.internal-api-key:secret-key-123}")
    private String internalApiKey;

    @Value("${app.security.x-api-key-enabled:false}")
    private boolean enabled;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Bỏ qua nếu cấu hình tắt hoặc là request từ Actuator/Health check
        String path = request.getRequestURI();
        return !enabled || path.contains("/actuator/") || path.contains("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestKey = request.getHeader(API_KEY_HEADER);

        if (internalApiKey.equals(requestKey)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Truy cập trái phép: Thiếu hoặc sai X-Internal-Api-Key từ [{}] cho [{}]", 
                    request.getRemoteAddr(), request.getRequestURI());
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or missing Internal API Key\"}");
        }
    }
}
