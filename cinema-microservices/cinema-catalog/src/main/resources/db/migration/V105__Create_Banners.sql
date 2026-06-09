-- Tạo bảng banners quảng cáo động
CREATE TABLE IF NOT EXISTS catalog.banners (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    image_url VARCHAR(1024) NOT NULL,
    link_url VARCHAR(1024),
    display_order INT NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
