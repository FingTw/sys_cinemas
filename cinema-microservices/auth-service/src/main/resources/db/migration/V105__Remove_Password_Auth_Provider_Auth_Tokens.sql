-- Xóa bảng auth_tokens vì hệ thống đã sử dụng Keycloak quản lý session
DROP TABLE IF EXISTS auth.auth_tokens;

-- Xóa các cột không còn sử dụng trong bảng users
ALTER TABLE auth.users 
    DROP COLUMN IF EXISTS password,
    DROP COLUMN IF EXISTS auth_provider;
