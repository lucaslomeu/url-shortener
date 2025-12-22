package org.lomeu.domain;

import java.time.ZonedDateTime;

public record Url (
    int id,
    String shortCode,
    String originalUrl,
    int clicks,
    ZonedDateTime creationDate,
    ZonedDateTime updatedAt)
{}
