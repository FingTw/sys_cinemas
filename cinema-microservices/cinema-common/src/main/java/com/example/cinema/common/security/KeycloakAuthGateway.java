// src/main/java/com/example/cinema/infrastructure/security/KeycloakAuthGateway.java
package com.example.cinema.common.security;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.example.cinema.common.security.ports.AuthGatewayPort;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class KeycloakAuthGateway implements AuthGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAuthGateway.class);

    @Value("${keycloak.token-uri}")
    private String keycloakTokenUri;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final RestClient restClient = RestClient.create();

    public boolean verifyCredentials(String username, String password) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("grant_type", "password");
            formData.add("username", username);
            formData.add("password", password);

            log.info("Bat dau gui Request xac thuc tai khoan [{}] len Keycloak: {}", username, keycloakTokenUri);

            ResponseEntity<Map> response = restClient.post()
                    .uri(keycloakTokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .toEntity(Map.class);

            // Neu Keycloak tra ve 200 OK (kem token cua no), tuc la User/Pass chuan
            log.info("Keycloak Xac thuc THANH CONG cho tai khoan: [{}]", username);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            // Keycloak tra ve 401 Unauthorized hoac loi khac
            log.warn("Xac thuc Keycloak THAT BAI cho tai khoan: [{}] - Loi: {}", username, e.getMessage());
            throw new RuntimeException("Loi xac thuc tu dich vu Keycloak: " + e.getMessage(), e);
        }
    }
}
