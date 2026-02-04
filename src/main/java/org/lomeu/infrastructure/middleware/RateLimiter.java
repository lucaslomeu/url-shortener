package org.lomeu.infrastructure.middleware;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int requestsPerMinute;

    public RateLimiter(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    public boolean allowRequest(String clientId) {
        Bucket bucket = buckets.computeIfAbsent(clientId, k -> createBucket());
        return bucket.tryConsume(1);
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(
            requestsPerMinute,
            Refill.intervally(requestsPerMinute, Duration.ofMinutes(1))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    public void cleanup() {
        if (buckets.size() > 10000) {
            buckets.clear();
        }
    }
}
