-- Tạo bảng cụm rạp
CREATE TABLE IF NOT EXISTS facility.cinema_complexes (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- Tạo bảng rạp chiếu phim
CREATE TABLE IF NOT EXISTS facility.cinemas (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    complex_id VARCHAR(255) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (complex_id) REFERENCES facility.cinema_complexes(id) ON DELETE CASCADE
);

-- Thêm cột cinema_id vào rooms
ALTER TABLE facility.rooms ADD COLUMN IF NOT EXISTS cinema_id VARCHAR(255);

-- Khởi tạo dữ liệu mặc định để liên kết phòng chiếu cũ
INSERT INTO facility.cinema_complexes (id, name, description)
VALUES ('default-complex', 'Hệ thống rạp Hà Nội', 'Mặc định cho các phòng chiếu hiện tại')
ON CONFLICT DO NOTHING;

INSERT INTO facility.cinemas (id, name, address, complex_id)
VALUES ('default-cinema', 'CGV Nguyễn Chí Thanh', '54A Nguyễn Chí Thanh, Láng Thượng, Đống Đa, Hà Nội', 'default-complex')
ON CONFLICT DO NOTHING;

-- Liên kết các phòng chiếu hiện hữu vào rạp mặc định
UPDATE facility.rooms SET cinema_id = 'default-cinema' WHERE cinema_id IS NULL;

-- Thiết lập ràng buộc NOT NULL và FK cho rooms.cinema_id
ALTER TABLE facility.rooms ALTER COLUMN cinema_id SET NOT NULL;
ALTER TABLE facility.rooms ADD CONSTRAINT fk_room_cinema FOREIGN KEY (cinema_id) REFERENCES facility.cinemas(id) ON DELETE CASCADE;
