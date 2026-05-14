-- 1. Tạo bảng permissions
CREATE TABLE IF NOT EXISTS auth.permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 2. Tạo bảng quan hệ Role - Permission (n-n)
CREATE TABLE IF NOT EXISTS auth.role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES auth.roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_permission FOREIGN KEY (permission_id) REFERENCES auth.permissions (id) ON DELETE CASCADE
);

-- 3. Tạo bảng quan hệ User - Permission (n-n) - Cho phép gán quyền trực tiếp cho user không qua role
CREATE TABLE IF NOT EXISTS auth.user_permissions (
    user_id VARCHAR(255) NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (user_id, permission_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES auth.users (id) ON DELETE CASCADE,
    CONSTRAINT fk_permission FOREIGN KEY (permission_id) REFERENCES auth.permissions (id) ON DELETE CASCADE
);

-- 4. Thêm các quyền cơ bản
INSERT INTO auth.permissions (name, description) VALUES 
('MOVIE_READ', 'Xem danh sách và chi tiết phim'),
('MOVIE_CREATE', 'Thêm phim mới'),
('MOVIE_UPDATE', 'Cập nhật thông tin phim'),
('MOVIE_DELETE', 'Xóa phim'),
('SHOWTIME_READ', 'Xem lịch chiếu'),
('SHOWTIME_CREATE', 'Tạo lịch chiếu mới'),
('SHOWTIME_UPDATE', 'Cập nhật lịch chiếu'),
('SHOWTIME_DELETE', 'Xóa lịch chiếu'),
('FACILITY_READ', 'Xem thông tin phòng chiếu/ghế'),
('FACILITY_CREATE', 'Thêm phòng chiếu/ghế'),
('FACILITY_UPDATE', 'Cập nhật phòng chiếu/ghế'),
('FACILITY_DELETE', 'Xóa phòng chiếu/ghế'),
('USER_READ', 'Xem danh sách người dùng'),
('USER_MANAGE', 'Quản lý người dùng (khóa, đổi quyền)'),
('BOOKING_CREATE', 'Đặt vé xem phim')
ON CONFLICT (name) DO NOTHING;

-- 5. Gán quyền cho các Role hiện có (Mapping từ RolePermissionMapper)

-- Gán quyền cho USER
INSERT INTO auth.role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM auth.roles r, auth.permissions p
WHERE r.name = 'USER' AND p.name IN ('MOVIE_READ', 'SHOWTIME_READ', 'FACILITY_READ', 'BOOKING_CREATE')
ON CONFLICT DO NOTHING;

-- Gán quyền cho STAFF (Nếu đã tồn tại role này)
INSERT INTO auth.roles (name) VALUES ('STAFF') ON CONFLICT DO NOTHING;
INSERT INTO auth.role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM auth.roles r, auth.permissions p
WHERE r.name = 'STAFF' AND p.name IN (
    'MOVIE_READ', 'MOVIE_CREATE', 'MOVIE_UPDATE',
    'SHOWTIME_READ', 'SHOWTIME_CREATE', 'SHOWTIME_UPDATE',
    'FACILITY_READ', 'FACILITY_CREATE', 'FACILITY_UPDATE',
    'USER_READ'
)
ON CONFLICT DO NOTHING;

-- Gán quyền cho ADMIN
INSERT INTO auth.role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM auth.roles r, auth.permissions p
WHERE r.name = 'ADMIN' AND p.name IN (
    'MOVIE_READ', 'MOVIE_CREATE', 'MOVIE_UPDATE', 'MOVIE_DELETE',
    'SHOWTIME_READ', 'SHOWTIME_CREATE', 'SHOWTIME_UPDATE', 'SHOWTIME_DELETE',
    'FACILITY_READ', 'FACILITY_CREATE', 'FACILITY_UPDATE', 'FACILITY_DELETE',
    'USER_READ', 'USER_MANAGE', 'BOOKING_CREATE'
)
ON CONFLICT DO NOTHING;
