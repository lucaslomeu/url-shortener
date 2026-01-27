package org.lomeu.application.port.out;

import org.lomeu.domain.Url;

import java.util.Optional;

public interface UrlRepository {

    Url create(Url url);

    long nextId();

    Optional<Url> findByShortCode(String shortCode);
}
