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
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                HandlerUtils.handleOptionsRequest(exchange);
                return;
            }

            HandlerUtils.setCorsHeaders(exchange);

            if ("POST".equals(exchange.getRequestMethod())) {
                try {

                    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        String jsonResponse = gson.toJson(Map.of("success", false, "message", "Missing or invalid auth token."));
                        HandlerUtils.sendJsonResponse(exchange, 401, jsonResponse); // 401 Unauthorized
                        return;
                    }

                    String token = authHeader.substring(7);
                    Claims claims = JwtUtil.validateToken(token);

                    if (claims == null) {
                        String jsonResponse = gson.toJson(Map.of("success", false, "message", "Invalid or expired token."));
                        HandlerUtils.sendJsonResponse(exchange, 401, jsonResponse);
                        return;
                    }

                    String username = claims.getSubject();

                    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                    Map<String, Object> requestBody = gson.fromJson(isr, Map.class);

                    int bikeId = ((Double) requestBody.get("bikeId")).intValue();

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
                exchange.sendResponseHeaders(405, -1);
            }
        } finally {
            exchange.close();
        }
    }
}