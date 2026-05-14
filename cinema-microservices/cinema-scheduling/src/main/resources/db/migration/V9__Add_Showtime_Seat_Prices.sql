-- Thêm cột giá cho từng hạng ghế vào bảng showtimes
ALTER TABLE scheduling.showtimes ADD COLUMN IF NOT EXISTS price_vip DECIMAL(19, 2) DEFAULT 120000;
ALTER TABLE scheduling.showtimes ADD COLUMN IF NOT EXISTS price_couple DECIMAL(19, 2) DEFAULT 195000;
