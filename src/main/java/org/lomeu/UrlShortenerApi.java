package org.lomeu;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class UrlShortenerApi {

    public static void start(final int port) throws Exception {
        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/shorten", new UrlShortener.PostHandler());
        server.createContext("/", new UrlShortener.GetHandler());

        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        // Start the server
        server.start();
        System.out.println("Server started on port 8080");
    }
}
