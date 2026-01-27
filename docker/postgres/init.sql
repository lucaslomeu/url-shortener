CREATE TABLE IF NOT EXISTS urls (
    id BIGINT PRIMARY KEY,

    short_code VARCHAR(10) NOT NULL UNIQUE,
    long_url TEXT NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_urls_short_code
ON urls (short_code);

CREATE INDEX IF NOT EXISTS idx_urls_long_url
ON urls (long_url);


