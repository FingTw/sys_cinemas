CREATE TABLE IF NOT EXISTS catalog.movies (
    id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INT,
    release_date DATE,
    poster_url VARCHAR(1000),
    genre VARCHAR(255),
    status VARCHAR(50) NOT NULL
);
