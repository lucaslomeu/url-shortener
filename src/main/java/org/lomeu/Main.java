package org.lomeu;

import org.lomeu.application.port.in.UrlUseCase;
import org.lomeu.application.port.out.UrlRepository;
import org.lomeu.application.service.UrlService;
import org.lomeu.infrastructure.database.JdbcUrlRepository;
import org.lomeu.infrastructure.http.UrlController;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        UrlRepository repo = new JdbcUrlRepository();
        UrlUseCase service = new UrlService(repo);

        UrlController.start(8080, service);
    }
}