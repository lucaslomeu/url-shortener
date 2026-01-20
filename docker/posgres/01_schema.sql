CREATE TABLE urls (
    id BIGSERIAL PRIMARY KEY,

    short_code VARCHAR(10) NOT NULL UNIQUE,
    original_url TEXT NOT NULL,
    original_url_hash CHAR(64) NOT NULL UNIQUE,

    clicks INT NOT NULL DEFAULT 0,

    creation_date TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
