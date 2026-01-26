package org.lomeu.application.service;

import org.lomeu.application.port.out.ShortCodeGeneratorPort;
import org.lomeu.application.port.in.UrlUseCase;
import org.lomeu.application.port.out.UrlRepository;
import org.lomeu.domain.Url;

import java.time.ZonedDateTime;
import java.util.UUID;

public class UrlService implements UrlUseCase {
    private final UrlRepository urlRepository;
    private final ShortCodeGeneratorPort codeGenerator;

    public UrlService(UrlRepository urlRepository, ShortCodeGeneratorPort codeGenerator) {
        this.urlRepository = urlRepository;
        this.codeGenerator = codeGenerator;
    }

    @Override
    public Url create(String longUrl) {
        String shortCode = this.codeGenerator.generate(8);
        UUID uniqueId = UUID.randomUUID();

        Url url = new Url(
            uniqueId.toString(),
            shortCode,
            longUrl,
            ZonedDateTime.now()
        );

        return urlRepository.create(url);
    }

    @Override
    public Url getByShortCode(String shortCode) {
        return urlRepository.findByShortCode(shortCode).orElse(null);
    }
}
