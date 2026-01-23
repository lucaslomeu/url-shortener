package org.lomeu.domain;

import java.time.ZonedDateTime;

public record Url (
    int id,
    String shortCode,
    String longUrl,
    ZonedDateTime createdAt)
{}