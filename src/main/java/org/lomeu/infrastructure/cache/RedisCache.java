package org.lomeu.infrastructure.cache;

import org.lomeu.application.port.out.CachePort;
import org.lomeu.domain.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Instant;
import java.util.Optional;

public class RedisCache implements CachePort {

    private static final int TTL_SECONDS = 300;
    private static final String KEY_PREFIX = "url:";

    private final JedisPool jedisPool;
    private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);

    public RedisCache(String host, int port) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(5);
        poolConfig.setTestOnBorrow(true);

        this.jedisPool = new JedisPool(poolConfig, host, port);
    }

    @Override
    public Optional<Url> get(String shortCode) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = KEY_PREFIX + shortCode;
            String value = jedis.get(key);

            if (value == null) {
                return Optional.empty();
            }

            String[] parts = value.split("\\|", 4);
            if (parts.length != 4) {
                return Optional.empty();
            }

            return Optional.of(new Url(
                Long.parseLong(parts[0]),
                parts[1],
                parts[2],
                Instant.parse(parts[3])
            ));

        } catch (Exception e) {
            logger.warn("Redis error on GET", e);
            return Optional.empty();
        }
    }

    @Override
    public void put(String shortCode, Url url) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = KEY_PREFIX + shortCode;

            String value = String.format("%d|%s|%s|%s",
                url.id(),
                url.shortCode(),
                url.longUrl(),
                url.createdAt().toString()
            );

            jedis.setex(key, TTL_SECONDS, value);

        } catch (Exception e) {
            logger.warn("Redis error on PUT", e);
        }
    }

    public void shutdown() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
        }
    }
}
