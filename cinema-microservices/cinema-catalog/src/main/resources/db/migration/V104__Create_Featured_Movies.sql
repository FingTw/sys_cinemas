-- Xóa cột is_featured từ bảng movies (vì chúng ta chuyển sang bảng riêng)
ALTER TABLE catalog.movies DROP COLUMN IF EXISTS is_featured;

-- Tạo bảng featured_movies
CREATE TABLE catalog.featured_movies (
    id VARCHAR(36) PRIMARY KEY,
    movie_id VARCHAR(36) NOT NULL,
    display_order INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_featured_movie FOREIGN KEY (movie_id) REFERENCES catalog.movies(id) ON DELETE CASCADE
);
