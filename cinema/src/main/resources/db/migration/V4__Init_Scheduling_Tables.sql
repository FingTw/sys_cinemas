CREATE TABLE IF NOT EXISTS scheduling.showtimes (
    id VARCHAR(255) PRIMARY KEY,
    movie_id VARCHAR(255) NOT NULL,
    room_id VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    price DECIMAL(19, 2) DEFAULT 75000
);

CREATE INDEX idx_showtimes_movie_id ON scheduling.showtimes(movie_id);
CREATE INDEX idx_showtimes_room_id ON scheduling.showtimes(room_id);
CREATE INDEX idx_showtimes_start_time ON scheduling.showtimes(start_time);
