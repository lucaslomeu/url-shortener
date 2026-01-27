package org.lomeu.domain;

import java.time.ZonedDateTime;

public record Url (
    Long id,
    String shortCode,
    String longUrl,
    ZonedDateTime createdAt)
{}