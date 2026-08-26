CREATE TABLE IF NOT EXISTS auth.auth_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    token_jti VARCHAR(255) NOT NULL UNIQUE,
    token_hash VARCHAR(512) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(512),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_auth_tokens_user_id ON auth.auth_tokens(user_id);
CREATE INDEX idx_auth_tokens_expires_at ON auth.auth_tokens(expires_at);
CREATE INDEX idx_auth_tokens_token_jti ON auth.auth_tokens(token_jti);
