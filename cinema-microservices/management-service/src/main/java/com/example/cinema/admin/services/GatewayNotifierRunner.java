package com.example.cinema.admin.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Runner chạy khi Admin Service khởi động xong, chịu trách nhiệm thông báo cho
 * Gateway đồng bộ lại Security và CORS với cơ chế tự động thử lại (Retry).
 */
@Component
public class GatewayNotifierRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GatewayNotifierRunner.class);

    private final JdbcTemplate jdbcTemplate;

    @Value("${GATEWAY_URL}")
    private String gatewayUrl;

    @Value("${APP_SECURITY_INTERNAL_API_KEY}")
    private String internalApiKey;

    @Autowired
    public GatewayNotifierRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        log.info(">> GatewayNotifierRunner: Ensuring checked_in column exists in bookings table...");
        try {
            jdbcTemplate.execute("ALTER TABLE booking.bookings ADD COLUMN IF NOT EXISTS checked_in BOOLEAN DEFAULT FALSE;");
            log.info(">> GatewayNotifierRunner: checked_in column check completed successfully.");
        } catch (Exception e) {
            log.error(">> GatewayNotifierRunner: Failed to run DB schema check for checked_in: {}", e.getMessage());
        }

        log.info(">> GatewayNotifierRunner: Admin service started. Initializing Gateway synchronization in background...");
        // Khởi chạy trong một thread phụ để tránh làm nghẽn tiến trình start của ứng dụng
        new Thread(this::notifyGatewayWithRetry).start();
    }

    private void notifyGatewayWithRetry() {
        int maxRetries = 5;
        int delayMs = 3000;
        boolean securitySynced = false;
        boolean corsSynced = false;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Api-Key", internalApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        for (int i = 1; i <= maxRetries; i++) {
            if (!securitySynced) {
                try {
                    restTemplate.exchange(gatewayUrl + "/internal/gateway/refresh-security", HttpMethod.POST, entity, Void.class);
                    log.info("Successfully notified API Gateway to refresh security configuration (Attempt {}).", i);
                    securitySynced = true;
                } catch (Exception e) {
                    log.warn("Failed to notify API Gateway to refresh security (Attempt {}): {}", i, e.getMessage());
                }
            }

            if (!corsSynced) {
                try {
                    restTemplate.exchange(gatewayUrl + "/internal/gateway/refresh-cors", HttpMethod.POST, entity, Void.class);
                    log.info("Successfully notified API Gateway to refresh CORS configuration (Attempt {}).", i);
                    corsSynced = true;
                } catch (Exception e) {
                    log.warn("Failed to notify API Gateway to refresh CORS (Attempt {}): {}", i, e.getMessage());
                }
            }

            if (securitySynced && corsSynced) {
                log.info(">> GatewayNotifierRunner: Gateway synchronization completed successfully. <<");
                return;
            }

            if (i < maxRetries) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Gateway notifier thread was interrupted: {}", e.getMessage());
                    return;
                }
            }
        }

        log.error(">> GatewayNotifierRunner: Failed to fully synchronize with Gateway after {} attempts. <<", maxRetries);
    }
}
