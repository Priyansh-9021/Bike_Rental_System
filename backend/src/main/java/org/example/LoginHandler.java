package org.example;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class LoginHandler implements HttpHandler {

    private final BikeRentalService bikeService;
    private final Gson gson = new Gson();

    public LoginHandler(BikeRentalService bikeService) {
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

                    boolean success = bikeService.login(username, password);

                    if (success) {
                        String token = JwtUtil.generateToken(username);

                        String jsonResponse = gson.toJson(Map.of(
                                "success", true,
                                "message", "Login successful!",
                                "token", token,
                                "username", username
                        ));
                        HandlerUtils.sendJsonResponse(exchange, 200, jsonResponse);
                    } else {
                        String jsonResponse = gson.toJson(Map.of("success", false, "message", "Invalid username or password."));
                        HandlerUtils.sendJsonResponse(exchange, 401, jsonResponse);
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