CREATE UNIQUE INDEX IF NOT EXISTS idx_urls_short_code
ON urls (short_code);

CREATE INDEX IF NOT EXISTS idx_urls_long_url
ON urls (long_url);