package org.lomeu.infrastructure.cache;

import org.lomeu.application.port.out.CachePort;
import org.lomeu.application.port.out.UrlRepository;
import org.lomeu.domain.Url;

import java.util.Optional;

public class CachedUrlRepository implements UrlRepository {

    private final UrlRepository delegate;
    private final CachePort cache;

    public CachedUrlRepository(UrlRepository delegate, CachePort cache) {
        this.delegate = delegate;
        this.cache = cache;
    }

    @Override
    public Optional<Url> findByShortCode(String shortCode) {
        Optional<Url> cached = cache.get(shortCode);
        if (cached.isPresent()) return cached;

        Optional<Url> fromDb = delegate.findByShortCode(shortCode);
        fromDb.ifPresent(url -> cache.put(shortCode, url));
        return fromDb;
    }

    @Override
    public Url insert(long id, String shortCode, String longUrl) {
        Url url = delegate.insert(id, shortCode, longUrl);
        cache.put(shortCode, url);
        return url;
    }

    @Override
    public long nextId() {
        return delegate.nextId();
    }
}
