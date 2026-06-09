package com.example.cinema.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * Filter chống trùng lặp yêu cầu (Idempotency).
 * Yêu cầu client gửi header "X-Idempotency-Key".
 * Key này được lưu trong Redis trong khoảng thời gian ngắn (ví dụ 10-30s).
 */
@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyFilter.class);
    private static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";

    private final StringRedisTemplate redisTemplate;

    public IdempotencyFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        // Thường chỉ áp dụng cho POST (tạo mới)
        return !"POST".equals(method);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String key = request.getHeader(IDEMPOTENCY_HEADER);

        if (key == null || key.trim().isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Tạo key duy nhất kết hợp với URI và có thể là UserId (nếu có)
        String redisKey = "idempotency:" + key;

        // Thử set vào Redis với TTL 30 giây
        // Nếu set thành công (chưa tồn tại) -> Boolean.TRUE
        Boolean isFirstRequest = redisTemplate.opsForValue().setIfAbsent(redisKey, "PROCESSING", Duration.ofSeconds(30));

        if (Boolean.FALSE.equals(isFirstRequest)) {
            log.warn("Phát hiện yêu cầu trùng lặp với Idempotency-Key: [{}]", key);
            response.setStatus(HttpStatus.CONFLICT.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Request already processed or in progress. Please wait.\"}");
            return;
        }

        try {
            filterChain.doFilter(request, response);
            // Sau khi thành công, có thể cập nhật trạng thái hoặc giữ nguyên TTL
            redisTemplate.opsForValue().set(redisKey, "COMPLETED", Duration.ofSeconds(30));
        } catch (Exception e) {
            // Nếu lỗi, xóa key để client có thể thử lại ngay lập tức
            redisTemplate.delete(redisKey);
            throw e;
        }
    }
}
