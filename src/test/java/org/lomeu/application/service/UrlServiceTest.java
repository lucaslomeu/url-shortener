package org.lomeu.application.service;

import org.junit.jupiter.api.Test;
import org.lomeu.application.port.out.ShortCodeGeneratorPort;
import org.lomeu.application.port.out.UrlRepository;
import org.lomeu.domain.Url;
import org.lomeu.domain.exception.InvalidUrlException;
import org.lomeu.domain.validator.UrlValidator;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UrlServiceTest {

    @Test
    void shouldCreateShortUrl() {
        FakeRepository repository = new FakeRepository();
        ShortCodeGeneratorPort generator = id -> "abc";
        UrlValidator validator = new UrlValidator();

        UrlService service = new UrlService(repository, generator, validator);
        Url url = service.create("https://example.com");

        assertEquals(1L, url.id());
        assertEquals("abc", url.shortCode());
        assertEquals("https://example.com", url.longUrl());
    }

    @Test
    void shouldRejectInvalidUrl() {
        FakeRepository repository = new FakeRepository();
        ShortCodeGeneratorPort generator = id -> "abc";
        UrlValidator validator = new UrlValidator();

        UrlService service = new UrlService(repository, generator, validator);

        assertThrows(InvalidUrlException.class, () -> service.create("javascript:alert(1)"));
    }

    private static class FakeRepository implements UrlRepository {
        private long nextId = 1;

        @Override
        public long nextId() {
            return nextId;
        }

        @Override
        public Url insert(long id, String shortCode, String longUrl) {
            return new Url(id, shortCode, longUrl, Instant.now());
        }

        @Override
        public Optional<Url> findByShortCode(String shortCode) {
            return Optional.empty();
        }
    }
}
