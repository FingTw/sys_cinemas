package com.example.cinema.presentation.handler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 1. Log request entry
        log.info("--> Incoming API Request: [{}] {}", method, uri);

        try {
            // 2. Chuyển request đến các layer tiếp theo
            filterChain.doFilter(request, response);
        } finally {
            // 3. Tính toán thời gian phản hồi (Response Time)
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            // 4. Log request exit với API response time
            log.info("<-- Outgoing API Response: [{}] {} - Status: {} - Time: {} ms", method, uri, status, duration);
        }
    }
}
