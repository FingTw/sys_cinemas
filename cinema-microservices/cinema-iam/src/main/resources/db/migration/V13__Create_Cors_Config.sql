CREATE TABLE cors_configs (
    id VARCHAR(36) PRIMARY KEY,
    allowed_origins VARCHAR(255) NOT NULL,
    allowed_methods VARCHAR(255) NOT NULL,
    allowed_headers VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP
);

INSERT INTO cors_configs (id, allowed_origins, allowed_methods, allowed_headers, updated_at) 
VALUES ('default-cors', '*', 'GET,POST,PUT,DELETE,OPTIONS', '*', CURRENT_TIMESTAMP);
