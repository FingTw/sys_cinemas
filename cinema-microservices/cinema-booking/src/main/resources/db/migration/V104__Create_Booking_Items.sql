-- =============================================
-- V104: Tạo bảng booking_items (F&B kèm đơn đặt vé)
-- =============================================

CREATE TABLE IF NOT EXISTS booking.booking_items (
    id              VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    booking_id      VARCHAR(36) NOT NULL REFERENCES booking.bookings(id),
    product_id      VARCHAR(36) NOT NULL,
    product_name    VARCHAR(200) NOT NULL,
    quantity        INT NOT NULL DEFAULT 1,
    unit_price      DECIMAL(12,0) NOT NULL,
    is_deleted      BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_booking_items_booking ON booking.booking_items(booking_id);
