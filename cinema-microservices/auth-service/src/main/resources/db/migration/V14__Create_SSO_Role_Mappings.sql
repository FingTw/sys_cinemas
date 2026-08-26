-- V14__Create_SSO_Role_Mappings.sql
CREATE TABLE IF NOT EXISTS auth.sso_role_mappings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sso_role_name VARCHAR(255) NOT NULL UNIQUE,
    local_role_id UUID NOT NULL,
    CONSTRAINT fk_sso_role_mapping_role FOREIGN KEY (local_role_id) REFERENCES auth.roles (id) ON DELETE CASCADE
);

-- Thêm các mapping mặc định dựa trên các role có sẵn
INSERT INTO auth.sso_role_mappings (sso_role_name, local_role_id)
SELECT 'admin', id FROM auth.roles WHERE name = 'ADMIN'
ON CONFLICT (sso_role_name) DO NOTHING;

INSERT INTO auth.sso_role_mappings (sso_role_name, local_role_id)
SELECT 'user', id FROM auth.roles WHERE name = 'USER'
ON CONFLICT (sso_role_name) DO NOTHING;

INSERT INTO auth.sso_role_mappings (sso_role_name, local_role_id)
SELECT 'staff', id FROM auth.roles WHERE name = 'STAFF'
ON CONFLICT (sso_role_name) DO NOTHING;
