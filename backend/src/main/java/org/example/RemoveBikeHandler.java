package org.example;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class RemoveBikeHandler implements HttpHandler {

    private final BikeRentalService bikeService;
    private final Gson gson = new Gson();

    public RemoveBikeHandler(BikeRentalService bikeService) {
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

                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                Map<String, Object> requestBody = gson.fromJson(isr, Map.class);
                int bikeId = ((Double) requestBody.get("bikeId")).intValue();
                String result = bikeService.removeBike(bikeId, username);

                if (result == null) {

                    HandlerUtils.sendJsonResponse(exchange, 200, gson.toJson(Map.of("success", true, "message", "Bike removed successfully.")));
                } else {
                    HandlerUtils.sendJsonResponse(exchange, 400, gson.toJson(Map.of("success", false, "message", result)));
                }

            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            HandlerUtils.sendJsonResponse(exchange, 500, gson.toJson(Map.of("success", false, "message", "Error processing request: " + e.getMessage())));
        } finally {
            exchange.close();
        }
    }
}