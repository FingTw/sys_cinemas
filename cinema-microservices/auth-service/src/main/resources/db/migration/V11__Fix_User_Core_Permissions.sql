-- Add missing permissions
INSERT INTO auth.permissions (name, description) VALUES
('BOOKING_READ', 'Xem lịch sử đặt vé cá nhân'),
('BOOKING_CANCEL', 'Hủy đơn đặt vé'),
('PROFILE_UPDATE', 'Cập nhật thông tin cá nhân')
ON CONFLICT (name) DO NOTHING;

-- Assign core permissions to USER role
INSERT INTO auth.role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM auth.roles r, auth.permissions p 
WHERE r.name = 'USER' 
AND p.name IN ('BOOKING_CREATE', 'BOOKING_READ', 'BOOKING_CANCEL', 'PROFILE_UPDATE')
ON CONFLICT DO NOTHING;

-- Ensure STAFF and ADMIN also have these core permissions
INSERT INTO auth.role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM auth.roles r, auth.permissions p 
WHERE r.name IN ('STAFF', 'ADMIN')
AND p.name IN ('BOOKING_CREATE', 'BOOKING_READ', 'BOOKING_CANCEL', 'PROFILE_UPDATE')
ON CONFLICT DO NOTHING;
