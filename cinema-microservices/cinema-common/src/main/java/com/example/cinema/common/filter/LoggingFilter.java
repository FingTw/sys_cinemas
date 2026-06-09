package com.example.cinema.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * HTTP Request/Response logging filter.
 * Gan moi request 1 Request ID duy nhat de tracing xuyen suot.
 *
 * MDC keys duoc set:
 * - requestId : UUID ngan de tracking
 * - service   : ten service dang xu ly
 *
 * Cac key nay tu dong xuat hien trong moi dong log
 * khi cau hinh log4j2 pattern co %X{requestId} va %X{service}.
 */
@Component
@Order(-100)
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Value("${spring.application.name}")
    private String serviceName;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // Tao Request ID (lay tu header neu co, nguoc lai generate moi)
        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        // Dat vao MDC de tat ca log trong request nay deu mang requestId
        MDC.put("requestId", requestId);
        MDC.put("service", serviceName.toUpperCase());

        long startTime = System.currentTimeMillis();

        try {
            logRequest(requestWrapper, requestId);
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Ghi response time vao MDC de hien thi trong log pattern
            MDC.put("responseTime", duration + "ms");

            logResponse(responseWrapper, requestId, duration);
            responseWrapper.copyBodyToResponse();

            // Don dep MDC sau moi request
            MDC.remove("requestId");
            MDC.remove("service");
            MDC.remove("responseTime");
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, String requestId) {
        String payload = getPayload(request.getContentAsByteArray(), request.getCharacterEncoding());
        String maskedPayload = com.example.cinema.common.util.LogMasker.mask(payload);
        log.info("[{}] --> {} {} | Client: {} | Payload: {}",
                requestId, request.getMethod(), request.getRequestURI(),
                request.getRemoteAddr(), maskedPayload.isEmpty() ? "(none)" : maskedPayload);
    }

    private void logResponse(ContentCachingResponseWrapper response, String requestId, long duration) {
        String payload = getPayload(response.getContentAsByteArray(), response.getCharacterEncoding());
        String maskedPayload = com.example.cinema.common.util.LogMasker.mask(payload);

        if (response.getStatus() >= 400) {
            log.warn("[{}] <-- Status: {} | Time: {}ms | Response: {}",
                    requestId, response.getStatus(), duration,
                    maskedPayload.isEmpty() ? "(none)" : truncate(maskedPayload, 500));
        } else {
            log.info("[{}] <-- Status: {} | Time: {}ms",
                    requestId, response.getStatus(), duration);
        }
    }

    private String getPayload(byte[] buf, String characterEncoding) {
        if (buf.length > 0) {
            try {
                return new String(buf, 0, buf.length, characterEncoding).replaceAll("\\s+", " ");
            } catch (UnsupportedEncodingException ex) {
                return "[Unknown]";
            }
        }
        return "";
    }

    /**
     * Cat bot payload qua dai de tranh log qua nhieu.
     */
    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...(truncated)";
    }
}
