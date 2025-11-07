package org.example;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims; // Make sure this is imported
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class ReturnHandler implements HttpHandler {

    private final BikeRentalService bikeService;
    private final Gson gson = new Gson();

    public ReturnHandler(BikeRentalService bikeService) {
        this.bikeService = bikeService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // --- 1. HANDLE CORS PRE-FLIGHT REQUEST ---
            // This block is essential
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                HandlerUtils.handleOptionsRequest(exchange);
                return; // Must return immediately
            }

            // --- 2. SET CORS HEADERS FOR THE ACTUAL REQUEST ---
            HandlerUtils.setCorsHeaders(exchange);

            if ("POST".equals(exchange.getRequestMethod())) {
                // --- 3. PROCESS THE POST REQUEST (WITH AUTH) ---
                try {
                    // Get the Authorization header
                    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        String jsonResponse = gson.toJson(Map.of("success", false, "message", "Missing or invalid auth token."));
                        HandlerUtils.sendJsonResponse(exchange, 401, jsonResponse); // 401 Unauthorized
                        return;
                    }

                    // Extract and validate the token
                    String token = authHeader.substring(7); // Remove "Bearer "
                    Claims claims = JwtUtil.validateToken(token);

                    if (claims == null) {
                        String jsonResponse = gson.toJson(Map.of("success", false, "message", "Invalid or expired token."));
                        HandlerUtils.sendJsonResponse(exchange, 401, jsonResponse); // 401 Unauthorized
                        return;
                    }

                    // Token is valid, get the username
                    String username = claims.getSubject();

                    // Read request body to get the bikeId
                    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                    Map<String, Object> requestBody = gson.fromJson(isr, Map.class);

                    int bikeId = ((Double) requestBody.get("bikeId")).intValue();

                    // Call the synchronized method
                    boolean success = bikeService.returnBike(bikeId, username);

                    if (success) {
                        String jsonResponse = gson.toJson(Map.of("success", true, "message", "Bike returned successfully!"));
                        HandlerUtils.sendJsonResponse(exchange, 200, jsonResponse);
                    } else {
                        String jsonResponse = gson.toJson(Map.of("success", false, "message", "Bike not found or was not rented."));
                        HandlerUtils.sendJsonResponse(exchange, 400, jsonResponse);
                    }
                } catch (Exception e) {
                    String jsonResponse = gson.toJson(Map.of("success", false, "message", "Error processing request: " + e.getMessage()));
                    HandlerUtils.sendJsonResponse(exchange, 500, jsonResponse);
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            }
        } finally {
            exchange.close(); // Ensure the exchange is always closed
        }
    }
}