CREATE TABLE password_policies (
    id VARCHAR(36) PRIMARY KEY,
    min_length INT NOT NULL DEFAULT 8,
    require_uppercase BOOLEAN NOT NULL DEFAULT true,
    require_lowercase BOOLEAN NOT NULL DEFAULT true,
    require_number BOOLEAN NOT NULL DEFAULT true,
    require_special_char BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMP
);

INSERT INTO password_policies (id, min_length, require_uppercase, require_lowercase, require_number, require_special_char, updated_at) 
VALUES ('default-policy', 8, true, true, true, true, CURRENT_TIMESTAMP);
