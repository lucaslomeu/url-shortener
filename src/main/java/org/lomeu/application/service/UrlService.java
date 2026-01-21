package org.lomeu.application.service;

import org.lomeu.application.port.out.HashPort;
import org.lomeu.application.port.out.ShortCodeGeneratorPort;
import org.lomeu.application.port.in.UrlUseCase;
import org.lomeu.application.port.out.UrlRepository;
import org.lomeu.domain.Url;

import java.time.ZonedDateTime;
import java.util.Optional;

public class UrlService implements UrlUseCase {
    private final UrlRepository urlRepository;
    private final HashPort hashPort;
    private final ShortCodeGeneratorPort codeGenerator;

    public UrlService(UrlRepository urlRepository, HashPort hashPort, ShortCodeGeneratorPort codeGenerator) {
        this.urlRepository = urlRepository;
        this.hashPort = hashPort;
        this.codeGenerator = codeGenerator;
    }

    @Override
    public Url create(String originalUrl) {
        String hash = hashPort.hash(originalUrl);

        Optional<Url> existing = urlRepository.findByOriginalUrlHash(hash);

        if (existing.isPresent()) {
            return existing.get();
        }

        while(true) {
            try {
                String shortCode = this.codeGenerator.generate(8);

                Url url = new Url(
                    0,
                    shortCode,
                    originalUrl,
                    hash,
                    0,
                    ZonedDateTime.now(),
                    ZonedDateTime.now()
                );

                return urlRepository.save(url);
            } catch (Exception e) {
                // Assume exception is due to shortCode collision, retry
            }
        }

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

}
