package org.lomeu.domain;

import java.time.ZonedDateTime;

public record Url (
    int id,
    String shortCode,
    String originalUrl,
    String originalUrlHash,
    int clicks,
    ZonedDateTime creationDate,
    ZonedDateTime updatedAt)
{}
