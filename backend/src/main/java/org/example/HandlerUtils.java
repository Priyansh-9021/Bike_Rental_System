package org.example;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;

public class HandlerUtils {

    /**
     * Sets the required CORS headers to allow your React frontend
     * (running on localhost:3000) to make requests.
     */
    public static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:3002");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
    }

    /**
     * Sends a JSON response back to the client.
     */
    public static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(jsonResponse.getBytes());
        os.close();
    }

    /**
     * Handles OPTIONS pre-flight requests from the browser.
     * This is necessary for CORS to work with POST requests.
     */
    public static void handleOptionsRequest(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        exchange.sendResponseHeaders(204, -1); // 204 No Content
    }
}