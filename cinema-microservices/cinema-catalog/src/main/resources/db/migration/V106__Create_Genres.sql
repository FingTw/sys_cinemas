-- Tạo bảng thể loại
CREATE TABLE IF NOT EXISTS catalog.genres (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    is_deleted BOOLEAN DEFAULT FALSE
);

-- Tạo bảng trung gian Many-to-Many giữa Movie và Genre
CREATE TABLE IF NOT EXISTS catalog.movie_genres (
    movie_id VARCHAR(255) NOT NULL,
    genre_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (movie_id, genre_id),
    FOREIGN KEY (movie_id) REFERENCES catalog.movies(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES catalog.genres(id) ON DELETE CASCADE
);

-- Tạo chỉ mục để tối ưu hóa truy vấn
CREATE INDEX IF NOT EXISTS idx_movie_genre_ids ON catalog.movie_genres(movie_id, genre_id);

-- Tự động migrate dữ liệu cũ từ cột movies.genre sang bảng genres và movie_genres mới
INSERT INTO catalog.genres (id, name, code)
SELECT DISTINCT
    LOWER(TRIM(val)) as id,
    TRIM(val) as name,
    LOWER(TRIM(val)) as code
FROM catalog.movies, regexp_split_to_table(genre, ',') as val
WHERE genre IS NOT NULL AND TRIM(val) <> ''
ON CONFLICT (code) DO NOTHING;

INSERT INTO catalog.movie_genres (movie_id, genre_id)
SELECT
    m.id as movie_id,
    g.id as genre_id
FROM catalog.movies m
CROSS JOIN LATERAL regexp_split_to_table(m.genre, ',') as val
JOIN catalog.genres g ON g.name = TRIM(val)
WHERE m.genre IS NOT NULL AND TRIM(val) <> ''
ON CONFLICT DO NOTHING;
