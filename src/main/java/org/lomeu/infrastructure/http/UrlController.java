package org.lomeu.infrastructure.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.lomeu.application.port.in.UrlUseCase;
import org.lomeu.config.AppConfig;
import org.lomeu.domain.Url;
import org.lomeu.domain.exception.InvalidUrlException;
import org.lomeu.infrastructure.cache.RedisCache;
import org.lomeu.infrastructure.database.Database;
import org.lomeu.infrastructure.http.dto.CreateShortUrlRequest;
import org.lomeu.infrastructure.middleware.RateLimiter;
import org.lomeu.infrastructure.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class UrlController {

    private static HttpServer server;
    private static final RateLimiter rateLimiter = new RateLimiter(AppConfig.rateLimitRequestsPerMinute());
    private static final Logger logger = LoggerFactory.getLogger(UrlController.class);
    private static RedisCache redisCache;
    private static final int MAX_BODY_BYTES = 8 * 1024;
    private static final AtomicLong totalRequests = new AtomicLong(0);
    private static final AtomicLong totalShortens = new AtomicLong(0);
    private static final AtomicLong totalRedirects = new AtomicLong(0);
    private static final AtomicLong totalErrors = new AtomicLong(0);

    public static void start(int port, UrlUseCase service, RedisCache cache) throws IOException {
        redisCache = cache;
        // Create HTTP server
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Endpoints with versioning
        String apiPrefix = "/api/v1";

        // Endpoint to create a shortened URL
        server.createContext(apiPrefix + "/shorten", exchange -> {
            totalRequests.incrementAndGet();
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            if (!allowRequest(exchange)) {
                sendRateLimitError(exchange);
                return;
            }

            try {
                String body = readRequestBody(exchange);

                CreateShortUrlRequest request = parse(body);

                Url url = service.create(request.url());
                totalShortens.incrementAndGet();
                sendJson(exchange, 201, "{\"shortCode\":\"" + escapeJson(url.shortCode()) + "\"}");
            } catch (PayloadTooLargeException e) {
                sendError(exchange, 413, "Request body too large");
            } catch (IllegalArgumentException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (InvalidUrlException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (DatabaseException e) {
                logger.error("Database error on shorten", e);
                sendError(exchange, 500, "Internal Server Error");
            } catch (Exception e) {
                logger.error("Unexpected error on shorten", e);
                sendError(exchange, 500, "Internal Server Error");
            }
        });

        // Endpoint to redirect to the original URL
        server.createContext(apiPrefix + "/", exchange -> {
            totalRequests.incrementAndGet();
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            if (!allowRequest(exchange)) {
                sendRateLimitError(exchange);
                return;
            }

            try {
                String path = exchange.getRequestURI().getPath();
                String shortCode = path.replace("/api/v1/", "");

                Optional<Url> url = service.getByShortCode(shortCode);

                if (url.isEmpty()) {
                    sendError(exchange, 404, "URL not found");
                    return;
                }

                totalRedirects.incrementAndGet();
                sendRedirect(exchange, url.get().longUrl());
            } catch (DatabaseException e) {
                logger.error("Database error on redirect", e);
                sendError(exchange, 500, "Internal Server Error");
            } catch (Exception e) {
                logger.error("Unexpected error on redirect", e);
                sendError(exchange, 500, "Internal Server Error");
            }
        });

        server.createContext("/", exchange -> {
            totalRequests.incrementAndGet();
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            if (!allowRequest(exchange)) {
                sendRateLimitError(exchange);
                return;
            }

            try {
                String path = exchange.getRequestURI().getPath();
                String shortCode = path.startsWith("/") ? path.substring(1) : path;

                if (shortCode.isBlank()) {
                    sendError(exchange, 404, "URL not found");
                    return;
                }

                Optional<Url> url = service.getByShortCode(shortCode);

                if (url.isEmpty()) {
                    sendError(exchange, 404, "URL not found");
                    return;
                }

                totalRedirects.incrementAndGet();
                sendRedirect(exchange, url.get().longUrl());
            } catch (DatabaseException e) {
                logger.error("Database error on root redirect", e);
                sendError(exchange, 500, "Internal Server Error");
            } catch (Exception e) {
                logger.error("Unexpected error on root redirect", e);
                sendError(exchange, 500, "Internal Server Error");
            }
        });

        server.createContext("/health", exchange -> {
            totalRequests.incrementAndGet();
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            boolean dbHealthy = Database.isHealthy();
            boolean redisHealthy = redisCache != null && redisCache.healthCheck();

            String status = (dbHealthy && redisHealthy) ? "UP" : "DOWN";
            int statusCode = (dbHealthy && redisHealthy) ? 200 : 503;

            String response = String.format(
                "{\"status\":\"%s\",\"database\":\"%s\",\"cache\":\"%s\"}",
                status,
                dbHealthy ? "UP" : "DOWN",
                redisHealthy ? "UP" : "DOWN"
            );

            sendJson(exchange, statusCode, response);
        });

        server.createContext("/metrics", exchange -> {
            totalRequests.incrementAndGet();
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }

            String response = String.format(
                "{\"requests\":%d,\"shortens\":%d,\"redirects\":%d,\"errors\":%d}",
                totalRequests.get(),
                totalShortens.get(),
                totalRedirects.get(),
                totalErrors.get()
            );
            sendJson(exchange, 200, response);
        });

        server.setExecutor(
            Executors.newVirtualThreadPerTaskExecutor()
        );

        server.start();
        logger.info("Server started on port {}", port);
    }

    public static void stop() {
        if (server != null) {
            logger.info("Stopping HTTP server...");
            server.stop(5);
            logger.info("HTTP server stopped");
        }
    }

    private static CreateShortUrlRequest parse(String body) {
        body = body.trim();

        if (!body.startsWith("{") || !body.endsWith("}")) {
            throw new IllegalArgumentException("JSON inválido");
        }

        body = body.substring(1, body.length() - 1);

        String[] parts = body.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("JSON inválido");
        }

        String key = parts[0].replace("\"", "").trim();
        String value = parts[1].replace("\"", "").trim();

        if (!key.equals("url") || value.isEmpty()) {
            throw new IllegalArgumentException("Campo 'url' inválido");
        }

        return new CreateShortUrlRequest(value);
    }


    private static void sendJson(HttpExchange ex, int status, String body)
            throws IOException {

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        addSecurityHeaders(ex);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }

        if (status >= 400) {
            totalErrors.incrementAndGet();
        }
    }

    private static String getClientIp(HttpExchange exchange) {
        String forwarded = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private static void sendRateLimitError(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Retry-After", "60");
        sendError(exchange, 429, "Too many requests. Please try again later.");
    }

    private static void sendError(HttpExchange exchange, int status, String message) throws IOException {
        String response = "{\"error\":\"" + escapeJson(message) + "\"}";
        sendJson(exchange, status, response);
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int total = 0;
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                total += read;
                if (total > MAX_BODY_BYTES) {
                    throw new PayloadTooLargeException();
                }
                outputStream.write(buffer, 0, read);
            }
            return outputStream.toString(StandardCharsets.UTF_8);
        }
    }

    private static boolean allowRequest(HttpExchange exchange) {
        rateLimiter.cleanup();
        String clientIp = getClientIp(exchange);
        return rateLimiter.allowRequest(clientIp);
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private static class PayloadTooLargeException extends RuntimeException {
        private PayloadTooLargeException() {
            super("Request body too large");
        }
    }

    private static void sendRedirect(HttpExchange exchange, String location) throws IOException {
        addSecurityHeaders(exchange);
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.getResponseBody().close();
    }

    private static void addSecurityHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("X-Frame-Options", "DENY");
        exchange.getResponseHeaders().set("X-XSS-Protection", "1; mode=block");
        exchange.getResponseHeaders().set("Strict-Transport-Security", "max-age=31536000");
        exchange.getResponseHeaders().set("Content-Security-Policy", "default-src 'self'");
        exchange.getResponseHeaders().set("Referrer-Policy", "no-referrer");
    }
}
