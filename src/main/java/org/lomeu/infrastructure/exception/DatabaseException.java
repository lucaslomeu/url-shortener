package org.lomeu.infrastructure.exception;

import org.lomeu.domain.exception.UrlShortenerException;

public class DatabaseException extends UrlShortenerException {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
