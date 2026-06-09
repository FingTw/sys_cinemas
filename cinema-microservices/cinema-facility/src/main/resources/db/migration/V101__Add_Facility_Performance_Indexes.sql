-- Index for filtering seats by room and deletion status
CREATE INDEX IF NOT EXISTS idx_seats_room_deleted ON facility.seats(room_id, is_deleted);

-- Index for room status and deletion status
CREATE INDEX IF NOT EXISTS idx_rooms_status_deleted ON facility.rooms(status, is_deleted);
