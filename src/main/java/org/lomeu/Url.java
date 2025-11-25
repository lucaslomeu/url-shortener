package org.lomeu;

public record Url (
        int id,
        String shortCode,
        String originalUrl,
        int clicks,
        String creationDate,
        String updatedAt)
{ }
