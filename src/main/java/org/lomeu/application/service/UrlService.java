package org.lomeu.application.service;

import org.lomeu.application.port.out.ShortCodeGeneratorPort;
import org.lomeu.application.port.in.UrlUseCase;
import org.lomeu.application.port.out.UrlRepository;
import org.lomeu.domain.Url;

import java.util.Objects;
import java.util.Optional;


public class UrlService implements UrlUseCase {

    private final UrlRepository urlRepository;
    private final ShortCodeGeneratorPort codeGenerator;

    public UrlService(UrlRepository urlRepository, ShortCodeGeneratorPort codeGenerator) {
        this.urlRepository = Objects.requireNonNull(urlRepository);
        this.codeGenerator = Objects.requireNonNull(codeGenerator);
    }

    @Override
    public Url create(String longUrl) {
        if (longUrl == null || longUrl.isBlank()) {
            throw new IllegalArgumentException("longUrl must not be blank");
        }

        long id = urlRepository.nextId();
        String shortCode = codeGenerator.generate(id);

        return urlRepository.insert(id, shortCode, longUrl);
    }

    @Override
    public Optional<Url> getByShortCode(String shortCode) {
        if (shortCode == null || shortCode.isBlank()) {
            return Optional.empty();
        }
        return urlRepository.findByShortCode(shortCode);
    }
}
