CREATE TABLE catalog.promotions (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(1024) NOT NULL,
    link_url VARCHAR(1024),
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE catalog.services (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(1024) NOT NULL,
    link_url VARCHAR(1024),
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert mock data for promotions
INSERT INTO catalog.promotions (id, title, description, image_url, link_url, display_order, active) VALUES
(gen_random_uuid()::varchar, 'ĐỒNG GIÁ 50K THỨ 2 CUỐI THÁNG', 'Ưu đãi cực sốc dành cho tất cả khách hàng đến xem phim vào ngày thứ 2 cuối cùng của tháng. Đồng giá chỉ 50.000 VNĐ cho mọi suất chiếu 2D.', 'https://cinestar.com.vn/pictures/c_monday.jpg', null, 1, true),
(gen_random_uuid()::varchar, 'HỌC SINH SINH VIÊN - ĐỒNG GIÁ 45K', 'Chỉ với 45.000 VNĐ, các bạn HSSV sẽ được thưởng thức những bộ phim bom tấn tại hệ thống rạp. Áp dụng cho suất chiếu 2D mọi khung giờ trong tuần.', 'https://cinestar.com.vn/pictures/Hình_nền_Màn_hình_Led_-_C_STUDENT.jpg', null, 2, true),
(gen_random_uuid()::varchar, 'C''TEN - ĐỒNG GIÁ 50K', 'Mua vé xem phim với giá ưu đãi cực sốc chỉ 50.000 VNĐ. Áp dụng cho các bạn có thẻ thành viên C''TEN.', 'https://cinestar.com.vn/pictures/C_Ten.jpg', null, 3, true);

-- Insert mock data for services
INSERT INTO catalog.services (id, title, description, image_url, link_url, display_order, active) VALUES
(gen_random_uuid()::varchar, 'SỰ KIỆN', null, 'https://cinestar.com.vn/pictures/billiards.jpg', null, 1, true),
(gen_random_uuid()::varchar, 'TỔ CHỨC TIỆC', null, 'https://cinestar.com.vn/pictures/nha-hang.jpg', null, 2, true),
(gen_random_uuid()::varchar, 'BOWLING', null, 'https://cinestar.com.vn/pictures/bowling.jpg', null, 3, true),
(gen_random_uuid()::varchar, 'KHU VUI CHƠI', null, 'https://cinestar.com.vn/pictures/kidzone.jpg', null, 4, true);
