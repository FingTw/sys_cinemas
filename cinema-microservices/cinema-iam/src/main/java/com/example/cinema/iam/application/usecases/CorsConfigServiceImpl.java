package com.example.cinema.iam.application.usecases;

import com.example.cinema.iam.application.ports.in.CorsConfigUseCase;
import com.example.cinema.iam.domain.entities.CorsConfig;
import com.example.cinema.iam.domain.repositories.CorsConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CorsConfigServiceImpl implements CorsConfigUseCase {

    private final CorsConfigRepository corsConfigRepository;

    @Value("${GATEWAY_URL}")
    private String gatewayUrl;

    @Value("${APP_SECURITY_INTERNAL_API_KEY}")
    private String internalApiKey;

    @Override
    @Transactional(readOnly = true)
    public CorsConfig getConfig() {
        return corsConfigRepository.findById("default-cors")
                .orElseGet(() -> new CorsConfig("default-cors", "*", "GET,POST,PUT,DELETE,OPTIONS", "*", LocalDateTime.now()));
    }

}
