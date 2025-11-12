package org.example;

import com.google.gson.Gson; // <-- MAKE SURE GSON IS IMPORTED
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BikeRentalService {

    // --- SHARED DATA ---
    private final Map<Integer, Bike> bikeInventory = new ConcurrentHashMap<>();
    private final List<Booking> bookings = new CopyOnWriteArrayList<>();
    private final Map<String, String> users = new ConcurrentHashMap<>(Map.of(
            "user", "pass123",
            "admin", "adminpass"
    ));
    private final AtomicInteger bikeIdCounter = new AtomicInteger(1000);

    // --- ADDED FOR WEBSOCKET ---
    private BikeWebSocketServer webSocketServer;
    private final Gson gson = new Gson();

    // --- ADDED FOR WEBSOCKET ---
    // This allows Main.java to give this service a reference to the websocket server
    public void setWebSocketServer(BikeWebSocketServer server) {
        this.webSocketServer = server;
    }

    // --- ADDED FOR WEBSOCKET ---
    // A helper method to broadcast updates to all clients
    private void broadcastUpdate() {
        if (webSocketServer != null) {
            // Send the entire fresh list of bikes to all clients
            String allBikesJson = gson.toJson(getAllBikes());
            webSocketServer.broadcast(allBikesJson);
        }
    }

    // --- BUSINESS LOGIC ---

    public boolean login(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }

    public synchronized boolean registerUser(String username, String password) {
        if (users.containsKey(username)) {
            return false; // User already exists
        }
        users.put(username, password);
        System.out.println("New user registered: " + username);
        return true;
    }

    // --- UPDATED with WebSocket hook ---
    public synchronized boolean bookBike(int bikeId, String userId) {
        Bike bike = bikeInventory.get(bikeId);
        if (bike != null && bike.isAvailable()) {
            bike.setAvailable(false);
            bike.setBookedBy(userId);
            bookings.add(new Booking(userId, bikeId, System.currentTimeMillis()));

            broadcastUpdate(); // <-- WebSocket call
            return true;
        }
        return false;
    }

    // --- UPDATED with WebSocket hook ---
    public synchronized boolean returnBike(int bikeId, String userId) {
        Bike bike = bikeInventory.get(bikeId);
        if (bike != null && !bike.isAvailable() && userId.equals(bike.getBookedBy())) {
            bike.setAvailable(true);
            bike.setBookedBy(null);

            broadcastUpdate(); // <-- WebSocket call
            return true;
        }
        return false;
    }

    // --- UPDATED with WebSocket hook ---
    public synchronized Bike listBike(String model, String location, String owner,
                                      int modelYear, double rentRate, String contactNumber, String photoUrl) {
        int newId = bikeIdCounter.incrementAndGet();
        Bike newBike = new Bike(newId, model, location, owner,
                modelYear, rentRate, contactNumber, photoUrl);
        bikeInventory.put(newId, newBike);

        broadcastUpdate(); // <-- WebSocket call
        return newBike;
    }

    // --- Other Methods ---
    public synchronized String removeBike(int bikeId, String userId) {
        Bike bike = bikeInventory.get(bikeId);

        // 1. Check if bike exists
        if (bike == null) {
            return "Bike not found.";
        }

        // 2. Check for ownership
        if (!bike.getOwner().equals(userId)) {
            return "You are not the owner of this bike and cannot remove it.";
        }

        // 3. Check if bike is currently rented
        if (!bike.isAvailable()) {
            return "Cannot remove a bike that is currently rented out.";
        }

        // All checks passed, remove the bike
        bikeInventory.remove(bikeId);
        System.out.println("User " + userId + " removed bike " + bikeId);

        broadcastUpdate(); // <-- WebSocket call
        return null; // <-- Success
    }

    public List<Bike> getBikesOwnedBy(String owner) {
        return bikeInventory.values().stream()
                .filter(bike -> bike.getOwner().equals(owner))
                .collect(Collectors.toList());
    }

    public List<Bike> getAvailableBikes() {
        return bikeInventory.values().stream()
                .filter(Bike::isAvailable)
                .collect(Collectors.toList());
    }

    public List<Bike> getAllBikes() {
        return List.copyOf(bikeInventory.values());
    }

    public void initializeBikes() {
        // Sample photo URL
        String defaultPhoto = "https://i.imgur.com/83S9Q4q.jpeg";

        bikeInventory.put(101, new Bike(102, "Electric Bike", "Beta", "admin",
                2024, 500.00, "555-1234", "data:image/jpeg;base64,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"));


        bikeIdCounter.set(102);
    }
}