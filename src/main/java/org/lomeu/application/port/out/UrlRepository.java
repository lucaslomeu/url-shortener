package org.lomeu.application.port.out;

import org.lomeu.domain.Url;

import java.util.Optional;

public interface UrlRepository {

    Url save(Url url);

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByOriginalUrlHash(String originalUrlHash);

    void incrementClicks(String shortCode);
}
