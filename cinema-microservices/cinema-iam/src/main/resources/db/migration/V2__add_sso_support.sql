-- ============================================================
-- Migration: Standalone Keycloak SSO Support
-- Mô tả: Thêm 2 cột phân biệt loại tài khoản (local vs SSO)
--         và cho phép password NULL (SSO-only users)
-- Chạy trên schema: auth
-- ============================================================

-- Bước 1: Cho phép password NULL (SSO user không có password)
ALTER TABLE auth.users
    ALTER COLUMN password DROP NOT NULL;

-- Bước 2: Thêm cột auth_provider — phân biệt nguồn gốc tài khoản
--   'local'    = Đăng ký thông thường qua form
--   'keycloak' = Đăng nhập SSO qua Keycloak
ALTER TABLE auth.users
    ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(50) NOT NULL DEFAULT 'local';

-- Bước 3: Thêm cột sso_subject — Keycloak Subject UUID (bất biến, dùng để link tài khoản)
ALTER TABLE auth.users
    ADD COLUMN IF NOT EXISTS sso_subject VARCHAR(255) UNIQUE;

-- Bước 4: Index tăng tốc tìm kiếm theo sso_subject (sparse index — chỉ cho SSO users)
CREATE INDEX IF NOT EXISTS idx_users_sso_subject
    ON auth.users (sso_subject)
    WHERE sso_subject IS NOT NULL;

-- Bước 5: Comment mô tả ý nghĩa cột
COMMENT ON COLUMN auth.users.auth_provider
    IS 'Nguon goc tai khoan: local = dang ky form; keycloak = SSO qua Keycloak';

COMMENT ON COLUMN auth.users.sso_subject
    IS 'Keycloak Subject UUID — bat bien trong suot vong doi tai khoan Keycloak. Dung de link tai khoan SSO.';

-- Kiểm tra: Các user cũ sẽ có auth_provider='local', sso_subject=NULL (đúng như mong muốn)
-- SELECT id, username, auth_provider, sso_subject FROM auth.users LIMIT 10;
