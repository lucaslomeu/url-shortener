CREATE UNIQUE INDEX IF NOT EXISTS idx_urls_short_code
ON urls (short_code);

CREATE INDEX IF NOT EXISTS idx_urls_original_url
ON urls (original_url);