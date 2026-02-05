package org.lomeu;

import org.lomeu.application.port.in.UrlUseCase;
import org.lomeu.application.port.out.ShortCodeGeneratorPort;
import org.lomeu.application.port.out.UrlRepository;
import org.lomeu.application.service.UrlService;
import org.lomeu.config.AppConfig;
import org.lomeu.infrastructure.cache.CachedUrlRepository;
import org.lomeu.infrastructure.cache.RedisCache;
import org.lomeu.infrastructure.database.Database;
import org.lomeu.infrastructure.database.JdbcUrlRepository;
import org.lomeu.infrastructure.generator.ShortCodeGenerator;
import org.lomeu.infrastructure.http.UrlController;
import org.lomeu.domain.validator.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

    private static RedisCache redisCache;
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down application...");
            UrlController.stop();
            Database.shutdown();
            if (redisCache != null) {
                redisCache.shutdown();
            }
            logger.info("Application stopped");
        }, "shutdown-hook"));

        redisCache = new RedisCache(AppConfig.redisHost(), AppConfig.redisPort());

        UrlRepository baseRepository = new JdbcUrlRepository();
        UrlRepository cachedRepository = new CachedUrlRepository(baseRepository, redisCache);

        ShortCodeGeneratorPort generator = new ShortCodeGenerator();

        UrlUseCase service = new UrlService(cachedRepository, generator, new UrlValidator());

        logger.info("Starting URL Shortener...");
        UrlController.start(8080, service, redisCache);
    }
}
