-- Thêm cột is_featured vào bảng movies
ALTER TABLE catalog.movies ADD COLUMN is_featured BOOLEAN DEFAULT FALSE;
