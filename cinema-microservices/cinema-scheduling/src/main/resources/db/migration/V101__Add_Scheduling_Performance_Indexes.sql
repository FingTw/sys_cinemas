-- Composite index for finding showtimes that have started (findByStatusAndStartTimeLessThanEqual)
CREATE INDEX IF NOT EXISTS idx_showtimes_status_start_time ON scheduling.showtimes(status, start_time);

-- Composite index for finding showtimes that have ended (findByStatusAndEndTimeLessThanEqual)
CREATE INDEX IF NOT EXISTS idx_showtimes_status_end_time ON scheduling.showtimes(status, end_time);

-- Composite index for detecting showtime scheduling conflicts in a room (findConflicts)
-- Optimized with a partial index filter since we only care about non-cancelled showtimes
CREATE INDEX IF NOT EXISTS idx_showtimes_room_times 
ON scheduling.showtimes(room_id, start_time, end_time, status) 
WHERE status <> 'CANCELLED';
