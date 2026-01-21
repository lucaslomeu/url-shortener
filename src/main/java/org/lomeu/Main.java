package org.lomeu;

import org.lomeu.application.port.in.UrlUseCase;
import org.lomeu.application.port.out.HashPort;
import org.lomeu.application.port.out.ShortCodeGeneratorPort;
import org.lomeu.application.port.out.UrlRepository;
import org.lomeu.application.service.UrlService;
import org.lomeu.infrastructure.crypto.Sha256Hasher;
import org.lomeu.infrastructure.database.JdbcUrlRepository;
import org.lomeu.infrastructure.generator.ShortCodeGenerator;
import org.lomeu.infrastructure.http.UrlController;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        UrlRepository repository = new JdbcUrlRepository();
        HashPort hasher = new Sha256Hasher();
        ShortCodeGeneratorPort generator = new ShortCodeGenerator();

        UrlUseCase service = new UrlService(repository, hasher, generator);

        UrlController.start(8080, service);
    }
}