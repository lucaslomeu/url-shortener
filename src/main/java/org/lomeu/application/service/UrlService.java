package org.lomeu.application.service;

import org.lomeu.application.port.out.ShortCodeGeneratorPort;
import org.lomeu.application.port.in.UrlUseCase;
import org.lomeu.application.port.out.UrlRepository;
import org.lomeu.domain.Url;

import java.time.ZonedDateTime;

public class UrlService implements UrlUseCase {
    private final UrlRepository urlRepository;
    private final ShortCodeGeneratorPort codeGenerator;

    public UrlService(UrlRepository urlRepository, ShortCodeGeneratorPort codeGenerator) {
        this.urlRepository = urlRepository;
        this.codeGenerator = codeGenerator;
    }

    @Override
    public Url create(String longUrl) {
        long id = urlRepository.nextId();

        String shortCode = this.codeGenerator.generate(id);

        Url url = new Url(
            id,
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
