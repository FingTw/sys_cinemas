package com.example.cinema.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequest(requestWrapper);
            logResponse(responseWrapper, duration);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String payload = getPayload(request.getContentAsByteArray(), request.getCharacterEncoding());
        log.info("Incoming Request: {} {} | Client IP: {} | Payload: {}", 
                request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), payload);
    }

    private void logResponse(ContentCachingResponseWrapper response, long duration) {
        String payload = getPayload(response.getContentAsByteArray(), response.getCharacterEncoding());
        log.info("Outgoing Response: Status: {} | Time: {}ms | Payload: {}", 
                response.getStatus(), duration, payload);
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
}
