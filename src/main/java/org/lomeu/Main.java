package org.lomeu;

public class Main {
    public static void main(String[] args)  {
        try {
            HttpServer.start(8080);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}