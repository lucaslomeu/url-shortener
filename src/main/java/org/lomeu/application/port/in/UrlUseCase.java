package org.lomeu.application.port.in;

import org.lomeu.domain.Url;

public interface UrlUseCase {

    Url create(String originalUrl);

    Url getByShortCode(String shortCode);

    Url getInfo(String shortCode);
}
