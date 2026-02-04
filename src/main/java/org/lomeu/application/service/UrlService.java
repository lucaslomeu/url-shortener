package org.lomeu.application.service;

import org.lomeu.application.port.out.ShortCodeGeneratorPort;
import org.lomeu.application.port.in.UrlUseCase;
import org.lomeu.application.port.out.UrlRepository;
import org.lomeu.domain.Url;
import org.lomeu.domain.validator.UrlValidator;

import java.util.Objects;
import java.util.Optional;


public class UrlService implements UrlUseCase {

    private final UrlRepository urlRepository;
    private final ShortCodeGeneratorPort codeGenerator;
    private final UrlValidator urlValidator;

    public UrlService(
        UrlRepository urlRepository,
        ShortCodeGeneratorPort codeGenerator,
        UrlValidator urlValidator
    ) {
        this.urlRepository = Objects.requireNonNull(urlRepository);
        this.codeGenerator = Objects.requireNonNull(codeGenerator);
        this.urlValidator = Objects.requireNonNull(urlValidator);
    }

    @Override
    public Url create(String longUrl) {
        urlValidator.validate(longUrl);

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
