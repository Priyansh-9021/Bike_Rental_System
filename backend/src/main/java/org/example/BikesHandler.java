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
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                HandlerUtils.handleOptionsRequest(exchange);
                return;
            }

            HandlerUtils.setCorsHeaders(exchange);

            if ("GET".equals(exchange.getRequestMethod())) {
                List<Bike> bikes = bikeService.getAllBikes();
                String jsonResponse = gson.toJson(bikes);
                HandlerUtils.sendJsonResponse(exchange, 200, jsonResponse);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        } finally {
            exchange.close();
        }
    }
}