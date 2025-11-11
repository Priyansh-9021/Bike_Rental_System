package org.example;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress; // Make sure this is imported
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {

    public static void main(String[] args) throws IOException {
        int httpPort = 8080;
        int wsPort = 8081;

        // 1. Create the shared service
        BikeRentalService bikeService = new BikeRentalService();
        bikeService.initializeBikes();

        // 2. Create and start the HTTP server

        // --- MODIFIED LINE ---
        // Change "localhost" to "0.0.0.0" to listen on all network interfaces
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", httpPort), 0);

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        httpServer.setExecutor(threadPoolExecutor);

        // --- (Set up all your HTTP routes) ---
        httpServer.createContext("/api/bikes", new BikesHandler(bikeService));
        httpServer.createContext("/api/book", new BookHandler(bikeService));
        httpServer.createContext("/api/return", new ReturnHandler(bikeService));
        httpServer.createContext("/api/login", new LoginHandler(bikeService));
        httpServer.createContext("/api/register", new RegisterHandler(bikeService));
        httpServer.createContext("/api/list-bike", new ListBikeHandler(bikeService));
        httpServer.createContext("/api/my-bikes", new MyBikesHandler(bikeService));

        httpServer.start(); // <-- Start HTTP server
        System.out.println("HTTP server started on port " + httpPort + " (listening on all interfaces)");

        // 3. Create and start the WebSocket server

        // --- MODIFIED LINE ---
        // Change "localhost" to "0.0.0.0"
        BikeWebSocketServer webSocketServer = new BikeWebSocketServer(new InetSocketAddress("0.0.0.0", wsPort));
        webSocketServer.start(); // This runs it on a new thread

        // 4. --- LINK THE TWO ---
        // Give the bike service a way to talk to the websocket server
        bikeService.setWebSocketServer(webSocketServer);
    }
}