package com.example.cinema.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Filter trace log toàn cục trên API Gateway.
 * Trích xuất hoặc sinh mới `X-Request-Id` để gắn vào MDC (để log của Gateway in ra được)
 * và chuyển tiếp xuống các microservices qua Request Header.
 */
@Component
public class RequestTracingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestTracingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 1. Lấy hoặc tạo mới Request ID
        String requestId = request.getHeaders().getFirst("X-Request-Id");
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = request.getHeaders().getFirst("x-correlation-id");
        }
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        String finalRequestId = requestId;
        
        // 2. Thêm header X-Request-Id để chuyển tiếp xuống các service phía sau
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Request-Id", finalRequestId)
                .build();

        long startTime = System.currentTimeMillis();

        // Log request đi vào
        try {
            MDC.put("requestId", finalRequestId);
            log.info("[{}] --> {} {}", finalRequestId, request.getMethod(), request.getURI().getPath());
        } finally {
            MDC.remove("requestId");
        }

        // 3. Gắn ngược Request ID về cho Client ở Response Header trước khi chạy chain (tránh lỗi ReadOnlyHttpHeaders sau khi commit)
        exchange.getResponse().getHeaders().add("X-Request-Id", finalRequestId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    
                    long duration = System.currentTimeMillis() - startTime;
                    try {
                        MDC.put("requestId", finalRequestId);
                        log.info("[{}] <-- Status: {} | Time: {}ms", finalRequestId, response.getStatusCode(), duration);
                    } finally {
                        MDC.remove("requestId");
                    }
                }));
    }

    @Override
    public int getOrder() {
        return -300; // Chạy thật sớm trước các filter nghiệp vụ khác
    }
}
