-- Index on movie status and soft delete status for active/coming soon lists
CREATE INDEX IF NOT EXISTS idx_movies_status_deleted ON catalog.movies(status, is_deleted);

-- Index on release date for sorting movies
CREATE INDEX IF NOT EXISTS idx_movies_release_date ON catalog.movies(release_date);
