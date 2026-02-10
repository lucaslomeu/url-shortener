package org.lomeu.infrastructure.cache;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lomeu.domain.Url;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class RedisCacheIT {

    @Container
    private static final GenericContainer<?> redis =
        new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private static RedisCache cache;

    @BeforeAll
    static void setup() {
        cache = new RedisCache(redis.getHost(), redis.getMappedPort(6379));
    }

    @AfterAll
    static void teardown() {
        if (cache != null) {
            cache.shutdown();
        }
        redis.stop();
    }

    @Test
    void shouldPutAndGetUrl() {
        Url url = new Url(1L, "abc", "https://example.com", Instant.now());

        cache.put("abc", url);
        Optional<Url> loaded = cache.get("abc");

        assertTrue(loaded.isPresent());
        assertEquals(url.longUrl(), loaded.get().longUrl());
        assertEquals(url.shortCode(), loaded.get().shortCode());
    }
}
