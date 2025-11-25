package org.lomeu;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

public class UrlShortener {
    // Post Handler
    public static class PostHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream inputStream = exchange.getRequestBody();
                String originalUrl;

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    originalUrl = reader.readLine();
                } catch (IOException e) {
                    exchange.sendResponseHeaders(500, -1);
                    return;
                }

                if (originalUrl == null || originalUrl.trim().isEmpty() || !isValidUrl(originalUrl.trim())) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }

                originalUrl = originalUrl.trim();
                String shortCode = UUID.randomUUID().toString().substring(0,6);

                Url url = new Url(0, shortCode, originalUrl, 0, "", "");
                Database.save(url);

                exchange.sendResponseHeaders(200, shortCode.getBytes().length);
                exchange.getResponseBody().write(shortCode.getBytes());
                exchange.getResponseBody().close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private boolean isValidUrl(String url) {
            Pattern pattern = Pattern.compile("^(https?://).+");
            return pattern.matcher(url).matches();
        }
    }

    public static class GetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                URI uri = exchange.getRequestURI();
                String path = uri.getPath();
                // Expecting /api/v1/{shortCode}
                String[] parts = path.split("/");
                if (parts.length >= 4) {
                    String shortCode = parts[3];
                    Url url = Database.findByShortCode(shortCode);
                    if (url != null) {
                        Database.incrementClicks(shortCode); // increment click count
                        exchange.getResponseHeaders().add("Location", url.originalUrl());
                        exchange.sendResponseHeaders(302, -1);
                    } else {
                        String response = "URL não encontrada";
                        exchange.sendResponseHeaders(404, response.getBytes().length);
                        exchange.getResponseBody().write(response.getBytes());
                        exchange.getResponseBody().close();
                    }
                } else {
                    exchange.sendResponseHeaders(400, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    public static class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                URI uri = exchange.getRequestURI();
                String path = uri.getPath();
                String[] parts = path.split("/");
                if (parts.length >= 5) {
                    String shortCode = parts[4];
                    Url url = Database.findByShortCode(shortCode);
                    if (url != null) {
                        String json = String.format("{\"shortCode\":\"%s\",\"originalUrl\":\"%s\",\"clicks\":%d,\"creationDate\":\"%s\",\"updatedAt\":\"%s\"}",
                                url.shortCode(), url.originalUrl(), url.clicks(), url.creationDate(), url.updatedAt());
                        exchange.getResponseHeaders().add("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, json.getBytes().length);
                        exchange.getResponseBody().write(json.getBytes());
                        exchange.getResponseBody().close();
                    } else {
                        String response = "URL não encontrada";
                        exchange.sendResponseHeaders(404, response.getBytes().length);
                        exchange.getResponseBody().write(response.getBytes());
                        exchange.getResponseBody().close();
                    }
                } else {
                    exchange.sendResponseHeaders(400, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
}
