package org.lomeu.infrastructure.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lomeu.domain.Url;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class JdbcUrlRepositoryIT {

    @Container
    private static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("urlshortener")
            .withUsername("postgres")
            .withPassword("postgres");

    private static HikariDataSource dataSource;

    @BeforeAll
    static void setup() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SEQUENCE IF NOT EXISTS urls_id_seq");
            statement.execute("""
                CREATE TABLE IF NOT EXISTS urls (
                  id BIGINT PRIMARY KEY DEFAULT nextval('urls_id_seq'),
                  short_code VARCHAR(10) NOT NULL UNIQUE,
                  long_url TEXT NOT NULL,
                  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
                )
                """);
        }
    }

    @AfterAll
    static void teardown() {
        if (dataSource != null) {
            dataSource.close();
        }
        postgres.stop();
    }

    @Test
    void shouldInsertAndFindByShortCode() {
        JdbcUrlRepository repository = new JdbcUrlRepository(dataSource);

        long id = repository.nextId();
        String shortCode = "t" + id;
        String longUrl = "https://example.com/" + id;

        Url inserted = repository.insert(id, shortCode, longUrl);
        Optional<Url> loaded = repository.findByShortCode(shortCode);

        assertTrue(loaded.isPresent());
        assertEquals(inserted.longUrl(), loaded.get().longUrl());
        assertEquals(inserted.shortCode(), loaded.get().shortCode());
    }
}
