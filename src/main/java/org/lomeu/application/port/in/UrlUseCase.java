package org.lomeu.application.port.in;

import org.lomeu.domain.Url;

import java.util.Optional;

public interface UrlUseCase {
    Url create(String longUrl);
    Optional<Url> getByShortCode(String shortCode);
}
