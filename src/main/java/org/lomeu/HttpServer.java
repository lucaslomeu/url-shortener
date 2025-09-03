package org.lomeu;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class HttpServer {

    public static void start(final int port) throws Exception {
        // Create HTTP server
        com.sun.net.httpserver.HttpServer httpServer = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);

        httpServer.createContext("/shorten", new UrlShortener.PostHandler());
        httpServer.createContext("/", new UrlShortener.GetHandler());

        httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        // Start the httpServer
        httpServer.start();
        System.out.println("Server started on port 8080");
    }
}
