-- Index for expired pending bookings (Scheduler query: status = 'PENDING' AND expires_at < NOW())
CREATE INDEX IF NOT EXISTS idx_bookings_status_expires_at ON booking.bookings(status, expires_at);

-- Index for looking up seats reserved/booked by showtime_id (existsByShowtimeIdAndSeatIdInAndStatusIn)
CREATE INDEX IF NOT EXISTS idx_booking_seats_showtime_id ON booking.booking_seats(showtime_id);

-- Index for payment callback / IPN lookups (findByPaymentTransactionId)
CREATE INDEX IF NOT EXISTS idx_bookings_payment_tx ON booking.bookings(payment_transaction_id);

-- Composite index for booking history queries ordered by creation date (findByUserIdOrderByCreatedAtDesc)
CREATE INDEX IF NOT EXISTS idx_bookings_user_created ON booking.bookings(user_id, created_at DESC);
