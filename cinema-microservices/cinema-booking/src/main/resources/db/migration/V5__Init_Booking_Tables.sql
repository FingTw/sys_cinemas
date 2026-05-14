CREATE TABLE IF NOT EXISTS booking.bookings (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    showtime_id VARCHAR(255) NOT NULL,
    total_price DECIMAL(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP NOT NULL,
    payment_transaction_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS booking.booking_seats (
    id VARCHAR(255) PRIMARY KEY,
    booking_id VARCHAR(255) NOT NULL,
    seat_id VARCHAR(255) NOT NULL,
    showtime_id VARCHAR(255) NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    CONSTRAINT fk_booking FOREIGN KEY (booking_id) REFERENCES booking.bookings(id) ON DELETE CASCADE,
    CONSTRAINT uk_seat_showtime UNIQUE (seat_id, showtime_id)
);

CREATE INDEX idx_bookings_user_id ON booking.bookings(user_id);
CREATE INDEX idx_bookings_showtime_id ON booking.bookings(showtime_id);
CREATE INDEX idx_booking_seats_booking_id ON booking.booking_seats(booking_id);
