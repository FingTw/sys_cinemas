package com.example.cinema.common.filter;

import com.example.cinema.common.util.CryptoUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Filter mã hoá phản hồi (Response) bằng AES trước khi trả về cho client.
 *
 * Chỉ mã hoá nếu request có Header "X-Response-Encrypt: true"
 * Hoặc cấu hình mặc định là mã hoá toàn bộ API.
 */
@Component
@Order(2) // Chạy sau LoggingFilter nhưng trước Security
public class AesEncryptionResponseFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AesEncryptionResponseFilter.class);

    @Value("${app.security.crypto-key}")
    private String cryptoKey;

    @Value("${app.security.encrypt-response:false}")
    private boolean encryptResponseEnabled;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Bỏ qua các path không phải API hoặc Swagger
        return !path.contains("/api/v1/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Kiểm tra xem request có yêu cầu mã hoá không (hoặc Global config)
        String encryptHeader = request.getHeader("X-Response-Encrypt");
        boolean shouldEncrypt = encryptResponseEnabled || "true".equalsIgnoreCase(encryptHeader);

        if (!shouldEncrypt) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(request, responseWrapper);

            byte[] responseData = responseWrapper.getContentAsByteArray();
            if (responseData.length > 0) {
                String originalJson = new String(responseData, StandardCharsets.UTF_8);

                // Thực hiện mã hoá
                String encryptedPayload = CryptoUtil.encrypt(originalJson, cryptoKey);
                
                // Bọc lại vào JSON format chuẩn: { "payload": "..." }
                Map<String, String> responseMap = Map.of("payload", encryptedPayload);
                byte[] encryptedBytes = objectMapper.writeValueAsBytes(responseMap);

                // Ghi đè vào response thực tế
                response.setContentType("application/json");
                response.setContentLength(encryptedBytes.length);
                response.getOutputStream().write(encryptedBytes);
                response.flushBuffer();

                log.debug("AesEncryptionResponseFilter: Đã mã hoá phản hồi cho {}", request.getRequestURI());
            }
        } catch (Exception e) {
            log.error("Lỗi mã hoá phản hồi: {}", e.getMessage());
            // Nếu lỗi mã hoá, trả về dữ liệu gốc để không làm hỏng app (hoặc throw tuỳ logic)
            responseWrapper.copyBodyToResponse();
        }
    }
}
