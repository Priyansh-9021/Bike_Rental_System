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
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                HandlerUtils.handleOptionsRequest(exchange);
                return;
            }

            HandlerUtils.setCorsHeaders(exchange);

            if ("POST".equals(exchange.getRequestMethod())) {
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

                    boolean success = bikeService.registerUser(username, password);

                    if (success) {
                        String jsonResponse = gson.toJson(Map.of("success", true, "message", "User registered successfully!"));
                        HandlerUtils.sendJsonResponse(exchange, 201, jsonResponse); // 201 Created
                    } else {
                        String jsonResponse = gson.toJson(Map.of("success", false, "message", "Username already exists."));
                        HandlerUtils.sendJsonResponse(exchange, 409, jsonResponse); // 409 Conflict
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