package org.lomeu.application.service;

import org.lomeu.application.port.in.UrlUseCase;
import org.lomeu.application.port.out.UrlRepository;
import org.lomeu.domain.Url;

import java.time.ZonedDateTime;

public class UrlService implements UrlUseCase {
    private final UrlRepository urlRepository;

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Override
    public Url create(String originalUrl) {
        // Implementation here
        String shortCode = generateShortCode();
        Url url = new Url(
            0,
            shortCode,
            originalUrl,
            0,
            ZonedDateTime.now(),
            ZonedDateTime.now()
        );

        return urlRepository.save(url);
    }

    @Override
    public Url getByShortCode(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode).orElse(null);
        if (url != null) {
            urlRepository.incrementClicks(shortCode);
        }
        return url;
    }

    @Override
    public Url getInfo(String shortCode) {
        return urlRepository.findByShortCode(shortCode).orElse(null);
    }

    private String generateShortCode() {
        // placeholder → entra Base62 depois
        return Long.toHexString(System.nanoTime());
    }
}
