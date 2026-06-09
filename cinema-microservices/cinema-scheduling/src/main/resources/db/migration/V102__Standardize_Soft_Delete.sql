-- Cập nhật dữ liệu NULL thành FALSE để tránh lỗi
UPDATE scheduling.showtimes SET is_deleted = FALSE WHERE is_deleted IS NULL;

-- Cấu hình NOT NULL và DEFAULT FALSE
ALTER TABLE scheduling.showtimes ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE scheduling.showtimes ALTER COLUMN is_deleted SET DEFAULT FALSE;
