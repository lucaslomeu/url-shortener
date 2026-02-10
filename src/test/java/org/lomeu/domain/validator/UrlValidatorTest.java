package org.lomeu.domain.validator;

import org.junit.jupiter.api.Test;
import org.lomeu.domain.exception.InvalidUrlException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UrlValidatorTest {

    private final UrlValidator validator = new UrlValidator();

    @Test
    void shouldAcceptValidHttpUrl() {
        assertDoesNotThrow(() -> validator.validate("https://example.com"));
    }

    @Test
    void shouldRejectInvalidScheme() {
        assertThrows(InvalidUrlException.class, () -> validator.validate("javascript:alert(1)"));
    }

    @Test
    void shouldRejectLocalhost() {
        assertThrows(InvalidUrlException.class, () -> validator.validate("http://localhost"));
    }

    @Test
    void shouldRejectPrivateIpv4() {
        assertThrows(InvalidUrlException.class, () -> validator.validate("http://10.0.0.1"));
    }

    @Test
    void shouldRejectTooLongUrl() {
        String longUrl = "https://example.com/" + "a".repeat(2050);
        assertThrows(InvalidUrlException.class, () -> validator.validate(longUrl));
    }
}
