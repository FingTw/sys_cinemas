package com.example.cinema.common.security;

import com.example.cinema.common.security.ports.AuthGatewayPort;
import com.example.cinema.common.security.ports.SsoUserInfo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * KeycloakAuthGateway - Adapter giao tiep voi Keycloak Standalone.
 *
 * <p>Chuc nang chinh:
 * <ul>
 *   <li>{@link #validateKeycloakToken}: Validate JWT offline qua JWKS (khong goi mang sau lan dau).</li>
 *   <li>{@link #logoutFromKeycloak}: Huy session Keycloak khi nguoi dung dang xuat.</li>
 *   <li>{@link #verifyCredentials}: Xac thuc truc tiep (legacy, giu lai cho admin tool).</li>
 * </ul>
 */
@Component
public class KeycloakAuthGateway implements AuthGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAuthGateway.class);

    @Value("${keycloak.token-uri:}")
    private String keycloakTokenUri;

    @Value("${keycloak.jwks-uri:}")
    private String keycloakJwksUri;

    @Value("${keycloak.logout-uri:}")
    private String keycloakLogoutUri;

    @Value("${keycloak.client-id:}")
    private String clientId;

    @Value("${keycloak.client-secret:}")
    private String clientSecret;

    private final RestClient restClient = RestClient.create();

    /**
     * JwtDecoder dung JWKS cua Keycloak de validate chu ky JWT offline.
     * Spring Security tu dong fetch va cache JWKS, tu dong refresh khi key rotate.
     */
    private JwtDecoder keycloakJwtDecoder;

    @PostConstruct
    void init() {
        if (keycloakJwksUri != null && !keycloakJwksUri.isBlank()) {
            this.keycloakJwtDecoder = NimbusJwtDecoder.withJwkSetUri(keycloakJwksUri).build();
            log.info("[Keycloak] JwtDecoder khoi tao thanh cong. JWKS URI: {}", keycloakJwksUri);
        }
    }

    // =========================================================================
    // Validate Keycloak JWT (OFFLINE — khong goi mang sau lan fetch JWKS dau tien)
    // =========================================================================

    /**
     * Validate Keycloak Access Token / ID Token offline bang JWKS.
     *
     * <p>Quy trinh:
     * <ol>
     *   <li>Spring fetch JWKS tu Keycloak lan dau (cache lai).</li>
     *   <li>Validate chu ky JWT bang public key trong JWKS.</li>
     *   <li>Kiem tra exp, iss, aud claims tu dong.</li>
     *   <li>Tra ve SsoUserInfo tu claims.</li>
     * </ol>
     *
     * @param keycloakJwt Keycloak Access Token nhan tu Frontend.
     * @return {@link SsoUserInfo} chua username, email, sub.
     * @throws org.springframework.security.oauth2.jwt.JwtException neu token khong hop le.
     */
    @Override
    public SsoUserInfo validateKeycloakToken(String keycloakJwt) {
        log.debug("[SSO] Bat dau validate Keycloak JWT offline.");

        if (keycloakJwtDecoder == null) {
            log.error("[SSO] keycloakJwtDecoder chua duoc khoi tao do thieu keycloak.jwks-uri");
            throw new IllegalStateException("keycloakJwtDecoder is not initialized (keycloak.jwks-uri is missing)");
        }

        Jwt decoded = keycloakJwtDecoder.decode(keycloakJwt);

        String sub = decoded.getSubject();
        // Keycloak mac dinh dung preferred_username
        String username = decoded.getClaimAsString("preferred_username");
        String email = decoded.getClaimAsString("email");

        if (username == null || username.isBlank()) {
            log.error("[SSO] JWT hop le nhung khong co 'preferred_username' claim. Sub: {}", sub);
            throw new RuntimeException(
                    "Keycloak token thieu claim 'preferred_username'. " +
                    "Kiem tra cau hinh Protocol Mapper tren Keycloak Admin Console.");
        }

        // Trich xuat Roles tu Keycloak token
        java.util.Set<String> roles = new java.util.HashSet<>();

        // 1. Realm-level roles
        java.util.Map<String, Object> realmAccess = decoded.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) realmAccess.get("roles");
            for (Object obj : list) {
                if (obj instanceof String) {
                    roles.add((String) obj);
                }
            }
        }

        // 2. Client-level roles cho clientId cau hinh
        java.util.Map<String, Object> resourceAccess = decoded.getClaimAsMap("resource_access");
        if (resourceAccess != null && clientId != null && !clientId.isBlank()) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> clientAccess = (java.util.Map<String, Object>) resourceAccess.get(clientId);
            if (clientAccess != null && clientAccess.get("roles") instanceof java.util.List) {
                java.util.List<?> list = (java.util.List<?>) clientAccess.get("roles");
                for (Object obj : list) {
                    if (obj instanceof String) {
                        roles.add((String) obj);
                    }
                }
            }
        }

        log.info("[SSO] Validate Keycloak JWT THANH CONG. Username: [{}], Sub: {}, Roles extracted: {}", username, sub, roles);
        return new SsoUserInfo(username, email, sub, roles);
    }

    // =========================================================================
    // Backchannel Logout — Huy Keycloak Session khi nguoi dung logout
    // =========================================================================

    /**
     * Goi Keycloak backchannel logout endpoint de huy session va refresh token phia Keycloak.
     * Su dung Keycloak Refresh Token da luu trong Redis luc SSO login.
     *
     * <p>Neu Keycloak khong phan hoi hoac loi, chi log warning — khong throw exception
     * vi local session da duoc huy thanh cong truoc do.
     *
     * @param keycloakRefreshToken Keycloak Refresh Token (lay tu Redis "kc_refresh:{userId}").
     */
    @Override
    public void logoutFromKeycloak(String keycloakRefreshToken) {
        log.info("[SSO] Goi Keycloak backchannel logout...");
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);
            form.add("refresh_token", keycloakRefreshToken);

            restClient.post()
                    .uri(keycloakLogoutUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();

            log.info("[SSO] Keycloak session huy thanh cong (backchannel logout).");
        } catch (Exception e) {
            // Khong throw — local session da bi huy truoc do, Keycloak failure chi la warning
            log.warn("[SSO] Keycloak backchannel logout that bai (bo qua): {}", e.getMessage());
        }
    }

    // =========================================================================
    // Legacy: Xac thuc truc tiep (Resource Owner Password Credentials)
    // =========================================================================

    /**
     * Xac thuc truc tiep username/password qua Keycloak (ROPC flow).
     * Method nay duoc giu lai cho admin tool. Khong dung cho luong login chinh.
     */
    @Override
    public boolean verifyCredentials(String username, String password) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("grant_type", "password");
            formData.add("username", username);
            formData.add("password", password);

            log.info("[Legacy] Xac thuc tai khoan [{}] qua Keycloak ROPC.", username);
            var response = restClient.post()
                    .uri(keycloakTokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .toEntity(Map.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("[Legacy] Xac thuc ROPC that bai cho [{}]: {}", username, e.getMessage());
            throw new RuntimeException("Loi xac thuc ROPC: " + e.getMessage(), e);
        }
    }
}
