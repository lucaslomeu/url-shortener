package org.lomeu;

import org.lomeu.application.port.in.UrlUseCase;
import org.lomeu.application.port.out.ShortCodeGeneratorPort;
import org.lomeu.application.port.out.UrlRepository;
import org.lomeu.application.service.UrlService;
import org.lomeu.infrastructure.database.Database;
import org.lomeu.infrastructure.database.JdbcUrlRepository;
import org.lomeu.infrastructure.generator.ShortCodeGenerator;
import org.lomeu.infrastructure.http.UrlController;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down application...");
            UrlController.stop();
            Database.shutdown();
            System.out.println("Application stopped");
        }, "shutdown-hook"));

        UrlRepository repository = new JdbcUrlRepository();
        ShortCodeGeneratorPort generator = new ShortCodeGenerator();

        UrlUseCase service = new UrlService(repository, generator);

        System.out.println("Starting URL Shortener...");
        UrlController.start(8080, service);
    }
}