package org.example;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class ListBikeHandler implements HttpHandler {

    private final BikeRentalService bikeService;
    private final Gson gson = new Gson();

    public ListBikeHandler(BikeRentalService bikeService) {
        this.bikeService = bikeService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // --- (CORS OPTIONS check) ---
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                HandlerUtils.handleOptionsRequest(exchange);
                return;
            }
            HandlerUtils.setCorsHeaders(exchange);

            if ("POST".equals(exchange.getRequestMethod())) {
                // --- (Authentication check) ---
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    HandlerUtils.sendJsonResponse(exchange, 401, gson.toJson(Map.of("success", false, "message", "Missing token.")));
                    return;
                }
                Claims claims = JwtUtil.validateToken(authHeader.substring(7));
                if (claims == null) {
                    HandlerUtils.sendJsonResponse(exchange, 401, gson.toJson(Map.of("success", false, "message", "Invalid token.")));
                    return;
                }
                String username = claims.getSubject();

                // --- UPDATED LOGIC ---
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");

                // Use Map<String, Object> to handle mixed types (String, Double, Integer)
                Map<String, Object> requestBody = gson.fromJson(isr, Map.class);

                String model = (String) requestBody.get("model");
                String location = (String) requestBody.get("location");
                String contactNumber = (String) requestBody.get("contactNumber");
                String photoUrl = (String) requestBody.get("photoUrl");

                // GSON parses all numbers as Double, so we must cast correctly
                int modelYear = ((Double) requestBody.get("modelYear")).intValue();
                double rentRate = (Double) requestBody.get("rentRate");

                if (model == null || location == null || model.isEmpty() || location.isEmpty()) {
                    HandlerUtils.sendJsonResponse(exchange, 400, gson.toJson(Map.of("success", false, "message", "Model and location are required.")));
                    return;
                }

                Bike newBike = bikeService.listBike(model, location, username,
                        modelYear, rentRate, contactNumber, photoUrl);

                HandlerUtils.sendJsonResponse(exchange, 201, gson.toJson(newBike));

            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            // Add a catch-all for parsing errors (e.g., if modelYear is not a number)
            HandlerUtils.sendJsonResponse(exchange, 500, gson.toJson(Map.of("success", false, "message", "Error processing request: " + e.getMessage())));
        } finally {
            exchange.close();
        }
    }
}