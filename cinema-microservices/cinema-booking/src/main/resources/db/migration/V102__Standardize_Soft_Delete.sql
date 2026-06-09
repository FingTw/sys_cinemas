-- Cập nhật dữ liệu NULL thành FALSE để tránh lỗi
UPDATE booking.bookings SET is_deleted = FALSE WHERE is_deleted IS NULL;
UPDATE booking.booking_seats SET is_deleted = FALSE WHERE is_deleted IS NULL;

-- Cấu hình NOT NULL và DEFAULT FALSE
ALTER TABLE booking.bookings ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE booking.bookings ALTER COLUMN is_deleted SET DEFAULT FALSE;

ALTER TABLE booking.booking_seats ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE booking.booking_seats ALTER COLUMN is_deleted SET DEFAULT FALSE;
