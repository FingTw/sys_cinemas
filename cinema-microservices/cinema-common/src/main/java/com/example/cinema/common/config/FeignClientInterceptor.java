package com.example.cinema.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientInterceptor implements RequestInterceptor {

    @Value("${app.security.api-key:}")
    private String apiKey;

    @Override
    public void apply(RequestTemplate template) {
        String requestId = MDC.get("requestId");
        if (requestId != null && !requestId.trim().isEmpty()) {
            template.header("X-Request-Id", requestId);
        }
        
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            template.header("X-API-Key", apiKey);
        }
    }
}
