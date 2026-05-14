-- Thêm cột grid_rows và grid_cols cho sơ đồ phòng chiếu
ALTER TABLE cinema.rooms ADD COLUMN IF NOT EXISTS grid_rows INTEGER DEFAULT 10;
ALTER TABLE cinema.rooms ADD COLUMN IF NOT EXISTS grid_cols INTEGER DEFAULT 15;

-- Bỏ ràng buộc NOT NULL trên capacity vì không còn sử dụng
ALTER TABLE cinema.rooms ALTER COLUMN capacity DROP NOT NULL;
