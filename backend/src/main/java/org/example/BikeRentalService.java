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

        bikeInventory.put(101, new Bike(101, "Mountain Bike", "Alpha", "admin",
                2023, 25.00, "555-1234", defaultPhoto));
        bikeInventory.put(102, new Bike(102, "Electric Bike", "Beta", "admin",
                2024, 40.00, "555-1234", "https://i.imgur.com/qc3Q1sP.jpeg"));
        bikeInventory.put(103, new Bike(103, "Road Bike", "Gamma", "admin",
                2022, 30.00, "555-1234", "https://i.imgur.com/fFVLwD9.jpeg"));
        bikeInventory.put(104, new Bike(104, "Mountain Bike", "Delta", "admin",
                2023, 25.00, "555-1234", defaultPhoto));

        bikeIdCounter.set(104);
    }
}