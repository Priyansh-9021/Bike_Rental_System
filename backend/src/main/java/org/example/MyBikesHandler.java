package org.example;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MyBikesHandler implements HttpHandler {

    private final BikeRentalService bikeService;
    private final Gson gson = new Gson();

    public MyBikesHandler(BikeRentalService bikeService) {
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

            if ("GET".equals(exchange.getRequestMethod())) {
                // --- AUTHENTICATION ---
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

                String username = claims.getSubject(); // The owner

                // --- LOGIC ---
                List<Bike> myBikes = bikeService.getBikesOwnedBy(username);
                HandlerUtils.sendJsonResponse(exchange, 200, gson.toJson(myBikes));

            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        } finally {
            exchange.close();
        }
    }
}