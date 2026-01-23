package org.lomeu.application.port.in;

import org.lomeu.domain.Url;

public interface UrlUseCase {
    Url create(String longUrl);

    Url getByShortCode(String shortCode);
}
