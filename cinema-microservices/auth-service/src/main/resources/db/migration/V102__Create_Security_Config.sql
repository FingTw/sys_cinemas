CREATE TABLE security_configs (
    id VARCHAR(36) PRIMARY KEY,
    client_key VARCHAR(255) NOT NULL,
    gateway_protected_paths VARCHAR(1000) NOT NULL,
    service_bypass_paths VARCHAR(1000) NOT NULL,
    updated_at TIMESTAMP
);

INSERT INTO security_configs (id, client_key, gateway_protected_paths, service_bypass_paths, updated_at)
VALUES (
    'default-security',
    'my-secret-dev-api-key',
    '/api/v1/auth/login,/api/v1/auth/register,/api/v1/auth/public-key,/api/v1/auth/refresh-token,/api/v1/public',
    '/actuator,/v3/api-docs,/swagger-ui,/api/v1/auth/public-key,/api/v1/movies,/api/v1/showtimes,/api/v1/rooms,/api/v1/vnpay',
    CURRENT_TIMESTAMP
);
