-- =============================================
-- V108: Tạo bảng sản phẩm F&B (Đồ ăn & Thức uống)
-- =============================================

-- Danh mục sản phẩm (Bắp, Nước, Combo, Snack...)
CREATE TABLE IF NOT EXISTS catalog.product_categories (
    id              VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    name            VARCHAR(100) NOT NULL UNIQUE,
    icon_url        VARCHAR(1024),
    display_order   INT DEFAULT 0,
    active          BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sản phẩm F&B
CREATE TABLE IF NOT EXISTS catalog.products (
    id              VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid()::text,
    category_id     VARCHAR(36) NOT NULL REFERENCES catalog.product_categories(id),
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    image_url       VARCHAR(1024),
    price           DECIMAL(12,0) NOT NULL,
    display_order   INT DEFAULT 0,
    active          BOOLEAN DEFAULT TRUE,
    is_deleted      BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_products_category ON catalog.products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_active ON catalog.products(active, is_deleted);

-- Seed dữ liệu mẫu danh mục
INSERT INTO catalog.product_categories (name, icon_url, display_order) VALUES
('Combo', null, 1),
('Bắp', null, 2),
('Nước ngọt', null, 3),
('Snack', null, 4)
ON CONFLICT (name) DO NOTHING;
