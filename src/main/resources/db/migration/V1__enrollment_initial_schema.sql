CREATE TABLE IF NOT EXISTS enrollments(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    total_price NUMERIC(6, 2) NOT NULL,
    city        TEXT NOT NULL,
    street      TEXT NOT NULL,
    house       INTEGER NOT NULL,
    apartment   INTEGER NOT NULL,
    course_line_items JSONB NOT NULL,
    status      TEXT NOT NULL,
    created_by  TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);