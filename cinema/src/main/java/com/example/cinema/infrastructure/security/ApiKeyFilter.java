package com.example.cinema.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZonedDateTime;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.security.api-key}")
    private String validApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Bỏ qua các endpoint không cần check API key (VD: Actuator, Swagger, VNPay IPN)
        if (path.startsWith("/actuator") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || path.contains("/vnpay/return")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-API-Key");
        String authHeader = request.getHeader("Authorization");

        // Nếu có Authorization (JWT) thì bỏ qua check API Key vì JWTAuthenticationFilter sẽ lo
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Nếu không có JWT, bắt buộc phải có API Key hợp lệ
        if (apiKey == null || !apiKey.equals(validApiKey)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            String json = String.format("{\"timestamp\": \"%s\", \"status\": 403, \"error\": \"Forbidden\", \"message\": \"Thieu hoac sai X-API-Key\"}", ZonedDateTime.now().toString());
            response.getWriter().write(json);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
