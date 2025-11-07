package org.example;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;

public class BikesHandler implements HttpHandler {

    private final BikeRentalService bikeService;
    private final Gson gson = new Gson();

    public BikesHandler(BikeRentalService bikeService) {
        this.bikeService = bikeService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // --- 1. HANDLE CORS PRE-FLIGHT REQUEST ---
            // This is now required because the GET request includes an Authorization header
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                HandlerUtils.handleOptionsRequest(exchange);
                return; // Must return immediately
            }

            // --- 2. SET CORS HEADERS FOR THE ACTUAL REQUEST ---
            HandlerUtils.setCorsHeaders(exchange);

            if ("GET".equals(exchange.getRequestMethod())) {
                // Get all bikes (you could change this to getAvailableBikes())
                List<Bike> bikes = bikeService.getAllBikes();
                String jsonResponse = gson.toJson(bikes);
                HandlerUtils.sendJsonResponse(exchange, 200, jsonResponse);
            } else {
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            }
        } finally {
            exchange.close(); // Ensure the exchange is always closed
        }
    }
}