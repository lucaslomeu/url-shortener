package org.lomeu.application.port.out;

import org.lomeu.domain.Url;

import java.util.Optional;

public interface CachePort {
    Optional<Url> get(String shortCode);
    void put(String shortCode, Url url);
}