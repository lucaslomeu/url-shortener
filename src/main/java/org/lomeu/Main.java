package org.lomeu;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws Exception {
        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/shorten", new UrlShortener.PostHandler());
        server.createContext("/", new UrlShortener.GetHandler());

        // Start the server
        server.start();
        System.out.println("Server started on port 8080");
    }
}