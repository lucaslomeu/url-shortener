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

public class UrlController {

    private static HttpServer server;
    private static final RateLimiter rateLimiter = new RateLimiter(AppConfig.rateLimitRequestsPerMinute());
    private static final Logger logger = LoggerFactory.getLogger(UrlController.class);
    private static RedisCache redisCache;

    public static void start(int port, UrlUseCase service, RedisCache cache) throws IOException {
        redisCache = cache;
        // Create HTTP server
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Endpoints with versioning
        String apiPrefix = "/api/v1";

        // Endpoint to create a shortened URL
        server.createContext(apiPrefix + "/shorten", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                send(exchange, 405, "Method Not Allowed");
                return;
            }

            String clientIp = getClientIp(exchange);
            if (!rateLimiter.allowRequest(clientIp)) {
                sendRateLimitError(exchange);
                return;
            }

            try {
                String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
                );

                CreateShortUrlRequest request = parse(body);

                Url url = service.create(request.url());
                send(exchange, 201, "{\"shortCode\":\"" + url.shortCode() + "\"}");
            } catch (InvalidUrlException e) {
                send(exchange, 400, e.getMessage());
            } catch (DatabaseException e) {
                logger.error("Database error on shorten", e);
                send(exchange, 500, "Internal Server Error");
            } catch (Exception e) {
                logger.error("Unexpected error on shorten", e);
                send(exchange, 500, "Internal Server Error");
            }
        });

        // Endpoint to redirect to the original URL
        server.createContext(apiPrefix + "/", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                send(exchange, 405, "Method Not Allowed");
                return;
            }

            String clientIp = getClientIp(exchange);
            if (!rateLimiter.allowRequest(clientIp)) {
                sendRateLimitError(exchange);
                return;
            }

            try {
                String path = exchange.getRequestURI().getPath();
                String shortCode = path.replace("/api/v1/", "");

                Optional<Url> url = service.getByShortCode(shortCode);

                if (url.isEmpty()) {
                    send(exchange, 404, "URL not found");
                    return;
                }

                sendRedirect(exchange, url.get().longUrl());
            } catch (DatabaseException e) {
                logger.error("Database error on redirect", e);
                send(exchange, 500, "Internal Server Error");
            } catch (Exception e) {
                logger.error("Unexpected error on redirect", e);
                send(exchange, 500, "Internal Server Error");
            }
        });

        server.createContext("/health", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                send(exchange, 405, "Method Not Allowed");
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

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            send(exchange, statusCode, response);
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


    private static void send(HttpExchange ex, int status, String body)
            throws IOException {

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
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
        String response = "{\"error\":\"Too many requests. Please try again later.\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Retry-After", "60");
        exchange.sendResponseHeaders(429, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private static void sendRedirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.getResponseBody().close();
    }
}
