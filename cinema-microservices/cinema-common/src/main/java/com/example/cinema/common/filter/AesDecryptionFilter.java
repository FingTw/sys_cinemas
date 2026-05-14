package com.example.cinema.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Filter giải mã AES payload từ frontend trước khi request vào Controller.
 *
 * Frontend (cryptoInterceptor) mã hoá body thành: { "payload": "<AES_Base64>" }
 * Filter này nhận diện format đó, giải mã và thay thế body bằng JSON gốc.
 *
 * Chỉ áp dụng cho POST, PUT, PATCH.
 * Bỏ qua các endpoint không có "payload" (ví dụ: VNPay callback gửi body
 * thường).
 */
@Component
@Order(1) // Chạy sớm, trước Security Filter
public class AesDecryptionFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AesDecryptionFilter.class);

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding"; // PKCS5 = PKCS7 trong Java

    @Value("${app.security.crypto-key}")
    private String cryptoKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        // Chỉ filter POST, PUT, PATCH
        return !("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Đọc body gốc
        byte[] rawBody = request.getInputStream().readAllBytes();

        if (rawBody.length == 0) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String bodyStr = new String(rawBody, StandardCharsets.UTF_8).trim();

            // Parse thử xem có phải { "payload": "..." } không
            Map<?, ?> bodyMap = objectMapper.readValue(bodyStr, Map.class);

            if (bodyMap.containsKey("payload") && bodyMap.size() == 1) {
                String encryptedPayload = (String) bodyMap.get("payload");
                String decryptedJson = decrypt(encryptedPayload);

                log.debug("AesDecryptionFilter: Đã giải mã payload cho {} {}",
                        request.getMethod(), request.getRequestURI());

                byte[] decryptedBytes = decryptedJson.getBytes(StandardCharsets.UTF_8);
                filterChain.doFilter(new BodyReplacedRequestWrapper(request, decryptedBytes), response);
                return;
            }
        } catch (Exception e) {
            // Body không phải JSON hoặc không có "payload" → giữ nguyên, tiếp tục
            log.debug("AesDecryptionFilter: Body không phải encrypted payload, giữ nguyên. Lý do: {}", e.getMessage());
        }

        // Trường hợp không cần decrypt → wrap lại để stream vẫn đọc được
        filterChain.doFilter(new BodyReplacedRequestWrapper(request, rawBody), response);
    }

    private String decrypt(String encryptedBase64) throws Exception {
        byte[] keyBytes = cryptoKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Wrapper để thay thế body của HttpServletRequest,
     * vì InputStream chỉ đọc được 1 lần.
     */
    private static class BodyReplacedRequestWrapper extends HttpServletRequestWrapper {

        private final byte[] body;

        public BodyReplacedRequestWrapper(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                }

                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}