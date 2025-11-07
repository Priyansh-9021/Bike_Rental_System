package org.example;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims; // <-- New import
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class BookHandler implements HttpHandler {

    private final BikeRentalService bikeService;
    private final Gson gson = new Gson();

    public BookHandler(BikeRentalService bikeService) {
        this.bikeService = bikeService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Handle CORS pre-flight OPTIONS request
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            HandlerUtils.handleOptionsRequest(exchange);
            return;
        }

        HandlerUtils.setCorsHeaders(exchange);

        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                // --- 1. AUTHENTICATION ---

                // Get the Authorization header
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    String jsonResponse = gson.toJson(Map.of("success", false, "message", "Missing or invalid auth token."));
                    HandlerUtils.sendJsonResponse(exchange, 401, jsonResponse); // 401 Unauthorized
                    exchange.close();
                    return;
                }

                // Extract and validate the token
                String token = authHeader.substring(7); // Remove "Bearer "
                Claims claims = JwtUtil.validateToken(token);

                if (claims == null) {
                    String jsonResponse = gson.toJson(Map.of("success", false, "message", "Invalid or expired token."));
                    HandlerUtils.sendJsonResponse(exchange, 401, jsonResponse); // 401 Unauthorized
                    exchange.close();
                    return;
                }

                // --- 2. TOKEN IS VALID, PROCEED WITH LOGIC ---

                // Get user identity FROM THE TOKEN, not from the request body
                String username = claims.getSubject();

                // Read request body to get the bikeId
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                Map<String, Object> requestBody = gson.fromJson(isr, Map.class);

                // GSON parses numbers as Double, so we need this conversion
                int bikeId = ((Double) requestBody.get("bikeId")).intValue();

                // Call the synchronized method using the authenticated username
                boolean success = bikeService.bookBike(bikeId, username);

                if (success) {
                    String jsonResponse = gson.toJson(Map.of("success", true, "message", "Bike booked successfully!"));
                    HandlerUtils.sendJsonResponse(exchange, 200, jsonResponse);
                } else {
                    String jsonResponse = gson.toJson(Map.of("success", false, "message", "Bike not available or not found."));
                    HandlerUtils.sendJsonResponse(exchange, 400, jsonResponse); // 400 Bad Request
                }

            } catch (Exception e) {
                String jsonResponse = gson.toJson(Map.of("success", false, "message", "Error processing request: " + e.getMessage()));
                HandlerUtils.sendJsonResponse(exchange, 500, jsonResponse); // 500 Internal Server Error
            } finally {
                exchange.close(); // Ensure the exchange is always closed
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            exchange.close();
        }
    }
}