package org.lomeu.domain.exception;

public class InvalidUrlException extends UrlShortenerException {
    public InvalidUrlException(String message) {
        super(message);
    }

    public InvalidUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
