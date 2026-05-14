CREATE TABLE IF NOT EXISTS auth.users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    active_token VARCHAR(2048),
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    token_version BIGINT NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS auth.roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS auth.user_roles (
    user_id VARCHAR(255) NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES auth.users (id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES auth.roles (id) ON DELETE CASCADE
);

INSERT INTO auth.roles (name) VALUES ('USER'), ('ADMIN') ON CONFLICT DO NOTHING;
