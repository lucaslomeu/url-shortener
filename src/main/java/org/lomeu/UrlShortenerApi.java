package org.lomeu;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class UrlShortenerApi {

    public static void start(final int port) throws Exception {
        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Endpoints with versioning
        String apiPrefix = "/api/v1";
        server.createContext(apiPrefix + "/shorten", new UrlShortener.PostHandler());
        server.createContext(apiPrefix + "/{shortCode}", new UrlShortener.GetHandler());
        server.createContext(apiPrefix + "/stats/{shortCode}", new UrlShortener.StatsHandler());

        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        // Start the server
        server.start();
        System.out.println("Server started on port 8080");
    }
}
