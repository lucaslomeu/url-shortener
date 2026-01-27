package org.lomeu.domain;

import java.time.Instant;

public record Url(
    long id,
    String shortCode,
    String longUrl,
    Instant createdAt
) {}