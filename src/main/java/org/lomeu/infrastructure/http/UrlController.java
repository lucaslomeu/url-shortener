package org.lomeu.infrastructure.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.lomeu.application.port.in.UrlUseCase;
import org.lomeu.domain.Url;

import java.io.*;
import java.net.InetSocketAddress;
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
            if ("POST".equals(exchange.getRequestMethod())) {
                if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    send(exchange, 405, "Method Not Allowed");
                    return;
                }

                // body simples: {"url":"https://google.com"}
                String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
                );

                String longUrl = extractUrl(body);
                Url url = service.create(longUrl);

                send(exchange, 201, "{\"shortCode\":\"" + url.shortCode() + "\"}");
            } else {
                exchange.sendResponseHeaders(405, -1);
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

    private static void send(HttpExchange ex, int status, String body)
            throws IOException {

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static boolean isValidUrl(String url) {
        Pattern pattern = Pattern.compile("^(https?://).+");
        return pattern.matcher(url).matches();
    }

    // parsing tosco proposital (sem lib) tenho q arrumar
    private static String extractUrl(String body) {
        int start = body.indexOf(":") + 2;
        int end = body.lastIndexOf("\"");
        return body.substring(start, end);
    }
}
