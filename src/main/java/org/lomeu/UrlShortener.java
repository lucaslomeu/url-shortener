package org.lomeu;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UrlShortener {
    private static final Map<String, String> urlMap = new HashMap<>();
    private static int counter = 1;

    // Post Handler
    public static class PostHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String originalUrl = new String(exchange.getRequestBody().readAllBytes());
                String shortUrl = "http://localhost:8080/" + counter++;
                urlMap.put(shortUrl, originalUrl);

                exchange.sendResponseHeaders(200, shortUrl.length());
                exchange.getResponseBody().write(shortUrl.getBytes());
                exchange.getResponseBody().close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    public static class GetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("CHEGOU AQUI");
            if ("GET".equals(exchange.getRequestMethod())) {
                String path = exchange.getRequestURI().toString();
                String originalUrl = urlMap.get("http://localhost:8080" + path);

                if (originalUrl != null) {
                    exchange.getResponseHeaders().add("Location", originalUrl);
                    exchange.sendResponseHeaders(302, -1); // Redirect
                } else {
                    exchange.sendResponseHeaders(404, -1); // Not Found
                }
                exchange.getResponseBody().close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }
}
