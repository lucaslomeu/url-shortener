package org.lomeu.domain.exception;

public abstract class UrlShortenerException extends RuntimeException {
    public UrlShortenerException(String message) {
        super(message);
    }

    public UrlShortenerException(String message, Throwable cause) {
        super(message, cause);
    }
}
