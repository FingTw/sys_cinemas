-- Cập nhật dữ liệu NULL thành FALSE để tránh lỗi
UPDATE facility.rooms SET is_deleted = FALSE WHERE is_deleted IS NULL;
UPDATE facility.seats SET is_deleted = FALSE WHERE is_deleted IS NULL;

-- Cấu hình NOT NULL và DEFAULT FALSE
ALTER TABLE facility.rooms ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE facility.rooms ALTER COLUMN is_deleted SET DEFAULT FALSE;

ALTER TABLE facility.seats ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE facility.seats ALTER COLUMN is_deleted SET DEFAULT FALSE;
