package org.example;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class RegisterHandler implements HttpHandler {

    private final BikeRentalService bikeService;
    private final Gson gson = new Gson();

    public RegisterHandler(BikeRentalService bikeService) {
        this.bikeService = bikeService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // --- 1. HANDLE CORS PRE-FLIGHT REQUEST ---
            // This block is essential for the browser to allow the POST request
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                HandlerUtils.handleOptionsRequest(exchange);
                return; // Must return immediately
            }

            // --- 2. SET CORS HEADERS FOR THE ACTUAL REQUEST ---
            HandlerUtils.setCorsHeaders(exchange);

            if ("POST".equals(exchange.getRequestMethod())) {
                // --- 3. PROCESS THE POST REQUEST ---
                try {
                    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                    Map<String, String> requestBody = gson.fromJson(isr, Map.class);

                    String username = requestBody.get("username");
                    String password = requestBody.get("password");

                    if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
                        String jsonResponse = gson.toJson(Map.of("success", false, "message", "Username and password cannot be empty."));
                        HandlerUtils.sendJsonResponse(exchange, 400, jsonResponse); // 400 Bad Request
                        return;
                    }

                    // Call the synchronized registration method
                    boolean success = bikeService.registerUser(username, password);

                    if (success) {
                        String jsonResponse = gson.toJson(Map.of("success", true, "message", "User registered successfully!"));
                        HandlerUtils.sendJsonResponse(exchange, 201, jsonResponse); // 201 Created
                    } else {
                        String jsonResponse = gson.toJson(Map.of("success", false, "message", "Username already exists."));
                        HandlerUtils.sendJsonResponse(exchange, 409, jsonResponse); // 409 Conflict
                    }
                } catch (Exception e) {
                    // Handle any internal server errors
                    String jsonResponse = gson.toJson(Map.of("success", false, "message", "Error processing request: " + e.getMessage()));
                    HandlerUtils.sendJsonResponse(exchange, 500, jsonResponse);
                }
            } else {
                // Not a POST or OPTIONS request
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            }
        } finally {
            exchange.close(); // Ensure the exchange is always closed
        }
    }
}