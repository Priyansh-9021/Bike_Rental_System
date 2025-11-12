package org.example;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {

    public static void main(String[] args) throws IOException {
        int httpPort = 8080;
        int wsPort = 8081;

        BikeRentalService bikeService = new BikeRentalService();
        bikeService.initializeBikes();

        HttpServer httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", httpPort), 0);

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        httpServer.setExecutor(threadPoolExecutor);

        httpServer.createContext("/api/bikes", new BikesHandler(bikeService));
        httpServer.createContext("/api/book", new BookHandler(bikeService));
        httpServer.createContext("/api/return", new ReturnHandler(bikeService));
        httpServer.createContext("/api/login", new LoginHandler(bikeService));
        httpServer.createContext("/api/register", new RegisterHandler(bikeService));
        httpServer.createContext("/api/list-bike", new ListBikeHandler(bikeService));
        httpServer.createContext("/api/my-bikes", new MyBikesHandler(bikeService));
        httpServer.createContext("/api/remove-bike", new RemoveBikeHandler(bikeService));

        httpServer.start(); // <-- Start HTTP server
        System.out.println("HTTP server started on port " + httpPort + " (listening on all interfaces)");


        BikeWebSocketServer webSocketServer = new BikeWebSocketServer(new InetSocketAddress("0.0.0.0", wsPort));
        webSocketServer.start();

        bikeService.setWebSocketServer(webSocketServer);
    }
}