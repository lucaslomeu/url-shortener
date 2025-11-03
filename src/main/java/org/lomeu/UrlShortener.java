package org.lomeu;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UrlShortener {
    private static final Map<String, String> urlMap = new HashMap<>();

    // Post Handler
    public static class PostHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream inputStream = exchange.getRequestBody();
               StringBuilder requestBody = new StringBuilder();
               String originalUrl;

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
                    originalUrl = reader.readLine();
                } catch (IOException e) {
                    // Handle the exception
                    e.printStackTrace();
                    return;
                }

                // Validate URL
                if (originalUrl.isEmpty()) {
                    exchange.sendResponseHeaders(400, -1); // Bad Request
                    return;
                }

                originalUrl = originalUrl.trim();
                String shortCode = UUID.randomUUID().toString().substring(0,6); // Simples hash

                Url url = new Url(0, shortCode, originalUrl);
                Database.save(url);

                String response = "Shortened URL: http://localhost:8080/" + shortCode;
                exchange.sendResponseHeaders(200, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    public static class GetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String path = exchange.getRequestURI().getPath().substring(1);
                Url url = Database.findByShortCode(path);

                if (url != null) {
                    exchange.getResponseHeaders().add("Location", url.originalUrl());
                    exchange.sendResponseHeaders(302, -1); // Redirect
                } else {
                    String response = "URL não encontrada";
                    exchange.sendResponseHeaders(404, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                    return;
                }
                exchange.getResponseBody().close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }
}
