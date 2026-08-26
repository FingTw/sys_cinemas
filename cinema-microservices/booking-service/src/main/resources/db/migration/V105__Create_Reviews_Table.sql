CREATE TABLE IF NOT EXISTS booking.reviews (
    id            VARCHAR(36)  NOT NULL DEFAULT gen_random_uuid()::text,
    booking_id    VARCHAR(255) NOT NULL,
    user_id       VARCHAR(255) NOT NULL,
    movie_id      VARCHAR(255) NOT NULL,
    rating        SMALLINT     NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment       TEXT,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT pk_reviews PRIMARY KEY (id),
    CONSTRAINT uq_reviews_booking UNIQUE (booking_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_movie_id ON booking.reviews (movie_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user_id  ON booking.reviews (user_id);
