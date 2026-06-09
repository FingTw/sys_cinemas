-- Cập nhật dữ liệu NULL thành FALSE để tránh lỗi
UPDATE catalog.movies SET is_deleted = FALSE WHERE is_deleted IS NULL;

-- Cấu hình NOT NULL và DEFAULT FALSE
ALTER TABLE catalog.movies ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE catalog.movies ALTER COLUMN is_deleted SET DEFAULT FALSE;
