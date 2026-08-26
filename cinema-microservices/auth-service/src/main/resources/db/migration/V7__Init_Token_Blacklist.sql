CREATE TABLE IF NOT EXISTS auth.token_blacklist (
    token VARCHAR(1000) PRIMARY KEY,
    expires_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_token_blacklist_expires_at ON auth.token_blacklist(expires_at);
