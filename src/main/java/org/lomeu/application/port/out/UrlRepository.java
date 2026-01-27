package org.lomeu.application.port.out;

import org.lomeu.domain.Url;

import java.util.Optional;

public interface UrlRepository {

    long nextId();

    Url insert(long id, String shortCode, String longUrl);

    Optional<Url> findByShortCode(String shortCode);
}
