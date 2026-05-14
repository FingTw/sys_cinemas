package com.example.cinema.presentation.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@RestControllerAdvice(basePackages = {"com.example.cinema.presentation.controllers", "com.example.cinema.presentation.handler"})
public class CryptoAdvice extends RequestBodyAdviceAdapter implements ResponseBodyAdvice<Object> {

    @Value("${app.security.crypto-key:CinemaSecretKeyForAes256Bits123!}")
    private String cryptoKey;

    private final ObjectMapper objectMapper;

    public CryptoAdvice() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ─────────────────────────────────────────────────────────────
    // Mã hoá Response
    // ─────────────────────────────────────────────────────────────
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Chỉ mã hoá nếu endpoint trả về JSON
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        String path = request.getURI().getPath();
        if (body == null || path.contains("/vnpay") || path.contains("/public-key")) {
            return body; // Bỏ qua VNPAY, Ping, và Public Key (nếu muốn)
        }
        
        try {
            String json = (body instanceof String) ? (String) body : objectMapper.writeValueAsString(body);
            String encrypted = encrypt(json);
            Map<String, String> responseMap = Map.of("payload", encrypted);
            
            // Nếu converter là StringHttpMessageConverter, phải trả về chuỗi JSON
            if (selectedConverterType.getName().contains("StringHttpMessageConverter")) {
                return objectMapper.writeValueAsString(responseMap);
            }
            return responseMap;
        } catch (Exception e) {
            e.printStackTrace();
            return body;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Giải mã Request
    // ─────────────────────────────────────────────────────────────
    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        try {
            byte[] body = inputMessage.getBody().readAllBytes();
            if (body.length == 0) return inputMessage;

            String jsonPayload = new String(body, StandardCharsets.UTF_8);
            Map map = objectMapper.readValue(jsonPayload, Map.class);
            if (map.containsKey("payload")) {
                String decrypted = decrypt(map.get("payload").toString());
                return new HttpInputMessage() {
                    @Override
                    public InputStream getBody() {
                        return new ByteArrayInputStream(decrypted.getBytes(StandardCharsets.UTF_8));
                    }

                    @Override
                    public org.springframework.http.HttpHeaders getHeaders() {
                        return inputMessage.getHeaders();
                    }
                };
            }
        } catch (Exception e) {
            // Log or ignore
        }
        return inputMessage;
    }

    // ─────────────────────────────────────────────────────────────
    // Tiện ích AES
    // ─────────────────────────────────────────────────────────────
    private String encrypt(String data) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(cryptoKey.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private String decrypt(String encryptedData) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(cryptoKey.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
