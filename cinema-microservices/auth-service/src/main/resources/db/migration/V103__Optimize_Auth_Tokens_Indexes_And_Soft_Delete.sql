-- 1. Cập nhật dữ liệu NULL thành FALSE để tránh lỗi khi SET NOT NULL
UPDATE auth.users SET is_deleted = FALSE WHERE is_deleted IS NULL;
UPDATE auth.roles SET is_deleted = FALSE WHERE is_deleted IS NULL;
UPDATE auth.permissions SET is_deleted = FALSE WHERE is_deleted IS NULL;
UPDATE auth.role_permissions SET is_deleted = FALSE WHERE is_deleted IS NULL;
UPDATE auth.auth_tokens SET is_deleted = FALSE WHERE is_deleted IS NULL;
UPDATE auth.token_blacklist SET is_deleted = FALSE WHERE is_deleted IS NULL;

-- 2. Cấu hình ràng buộc NOT NULL và giá trị mặc định DEFAULT FALSE cho tất cả các bảng
ALTER TABLE auth.users ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE auth.users ALTER COLUMN is_deleted SET DEFAULT FALSE;

ALTER TABLE auth.roles ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE auth.roles ALTER COLUMN is_deleted SET DEFAULT FALSE;

ALTER TABLE auth.permissions ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE auth.permissions ALTER COLUMN is_deleted SET DEFAULT FALSE;

ALTER TABLE auth.role_permissions ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE auth.role_permissions ALTER COLUMN is_deleted SET DEFAULT FALSE;

ALTER TABLE auth.auth_tokens ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE auth.auth_tokens ALTER COLUMN is_deleted SET DEFAULT FALSE;

ALTER TABLE auth.token_blacklist ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE auth.token_blacklist ALTER COLUMN is_deleted SET DEFAULT FALSE;

-- 3. Tối ưu hóa Index cho Token đang hoạt động (Active Tokens)
DROP INDEX IF EXISTS auth.idx_auth_tokens_active_deleted;

-- Tạo Partial Indexes (chỉ mục một phần) tối ưu hiệu năng
CREATE INDEX IF NOT EXISTS idx_active_auth_tokens_hash 
ON auth.auth_tokens(token_hash) 
WHERE is_active = TRUE AND is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_active_auth_tokens_jti 
ON auth.auth_tokens(token_jti) 
WHERE is_active = TRUE AND is_deleted = FALSE;
