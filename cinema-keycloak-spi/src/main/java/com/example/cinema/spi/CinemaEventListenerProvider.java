package com.example.cinema.spi;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * CinemaEventListenerProvider — Lang nghe su kien tu Keycloak Admin.
 *
 * <p>Khi admin thay doi trang thai user tren Keycloak (xoa, vo hieu hoa, kich hoat lai),
 * SPI nay se goi HTTP ve cinema-iam de dong bo trang thai tai khoan trong app DB.
 *
 * <p>Nguyen tac quan trong:
 * <ul>
 *   <li>Chi SET is_blocked = true/false, KHONG bao gio xoa du lieu nghiep vu.</li>
 *   <li>Quyet dinh xoa du lieu phai do Admin thuc hien rieng trong cinema-admin.</li>
 * </ul>
 */
public class CinemaEventListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;

    // URL cua cinema-iam internal endpoint
    // Dung bien moi truong de tranh hardcode
    private static final String CINEMA_IAM_URL = getEnvOrDefault(
            "CINEMA_IAM_INTERNAL_URL",
            "http://cinema-iam:8081"
    );
    private static final String EVENT_ENDPOINT = CINEMA_IAM_URL + "/api/v1/internal/sso/user-event";

    // API Key noi bo (phai khop voi cau hinh cua cinema-iam)
    private static final String INTERNAL_API_KEY = getEnvOrDefault(
            "CINEMA_INTERNAL_API_KEY",
            "internal-secret-key"
    );

    public CinemaEventListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    /**
     * Lang nghe User events (dang nhap, dang xuat, v.v.)
     * Hien tai chi log, khong can xu ly gi them.
     */
    @Override
    public void onEvent(Event event) {
        // Khong can xu ly user-level events cho muc dich sync
        // Chi xu ly Admin events phia duoi
    }

    /**
     * Lang nghe Admin events — su kien khi admin thay doi user tren Keycloak Admin Console.
     */
    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        // Chi xu ly khi admin thao tac tren resource USER
        if (adminEvent.getResourceType() != ResourceType.USER) {
            return;
        }

        String resourcePath = adminEvent.getResourcePath(); // "users/{keycloak-user-id}"
        if (resourcePath == null || !resourcePath.startsWith("users/")) {
            return;
        }

        String keycloakUserId = resourcePath.replace("users/", "").split("/")[0];
        OperationType opType = adminEvent.getOperationType();

        System.err.println("=== EventListener: Admin event [" + opType + "] for user: " + keycloakUserId);

        String eventType = null;
        switch (opType) {
            case DELETE:
                // User bi xoa khoi Keycloak — block trong app DB (khong xoa du lieu)
                eventType = "USER_DISABLE";
                break;
            case UPDATE:
                // Kiem tra xem update co phai la disable/enable khong
                // Keycloak gui representation JSON khi update
                if (includeRepresentation && adminEvent.getRepresentation() != null) {
                    String repr = adminEvent.getRepresentation();
                    if (repr.contains("\"enabled\":false")) {
                        eventType = "USER_DISABLE";
                    } else if (repr.contains("\"enabled\":true")) {
                        eventType = "USER_ENABLE";
                    }
                }
                break;
            default:
                return; // CREATE, ACTION — khong can xu ly
        }

        if (eventType != null) {
            notifyCinemaIam(keycloakUserId, eventType);
        }
    }

    /**
     * Goi HTTP POST ve cinema-iam de dong bo trang thai user.
     *
     * @param ssoSubject  Keycloak User ID (= sso_subject trong app DB)
     * @param eventType   Loai su kien: "USER_DISABLE" hoac "USER_ENABLE"
     */
    private void notifyCinemaIam(String ssoSubject, String eventType) {
        try {
            String payload = "{\"type\":\"" + eventType + "\",\"ssoSubject\":\"" + ssoSubject + "\"}";

            URL url = new URL(EVENT_ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-Internal-Api-Key", INTERNAL_API_KEY);
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            System.err.println("=== EventListener: cinema-iam response [" + responseCode + "] for event ["
                    + eventType + "] user [" + ssoSubject + "]");

            conn.disconnect();
        } catch (Exception e) {
            // Khong de loi nay lam hong Keycloak — chi log va tiep tuc
            System.err.println("=== EventListener ERROR: Khong the goi cinema-iam: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        // Khong can dong tai nguyen gi
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
