package org.lomeu.infrastructure.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.lomeu.application.port.in.UrlUseCase;
import org.lomeu.domain.Url;
import org.lomeu.infrastructure.http.dto.CreateShortUrlRequest;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class UrlController {

    public static void start(int port, UrlUseCase service) throws IOException {
        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Endpoints with versioning
        String apiPrefix = "/api/v1";

        // Endpoint to create a shortened URL
        server.createContext(apiPrefix + "/shorten", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                send(exchange, 405, "Method Not Allowed");
                return;
            }
            try {
                String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
                );

                CreateShortUrlRequest request = parse(body);

                if (request.url().isEmpty() || !isValidUrl(request.url())) {
                    send(exchange, 400, "Invalid URL");
                    return;
                }
                Url url = service.create(request.url());
                send(exchange, 201, "{\"shortCode\":\"" + url.shortCode() + "\"}");
            } catch (Exception e) {
                e.printStackTrace(); // Log error for debugging
                send(exchange, 500, "Internal Server Error");
            }
        });

        // Endpoint to redirect to the original URL
        server.createContext(apiPrefix + "/", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                send(exchange, 405, "Method Not Allowed");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String shortCode = path.replace("/api/v1/", "");

            Url url = service.getByShortCode(shortCode);

            send(exchange, 302, url.longUrl());
        });

        server.setExecutor(
            Executors.newVirtualThreadPerTaskExecutor()
        );

        server.start();
        System.out.println("Server started on port " + port);
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

    private static boolean isValidUrl(String url) {
        try {
            new URI(url);
            return true;
        }  catch (URISyntaxException e) {
            return false;
        }
    }

    // parsing tosco proposital (sem lib) tenho q arrumar
    private static String extractUrl(String body) {
        int start = body.indexOf(":") + 2;
        int end = body.lastIndexOf("\"");
        return body.substring(start, end);
    }
}
