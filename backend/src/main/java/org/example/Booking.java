package org.example;

public class Booking {
    private String userId;
    private int bikeId;
    private long bookingTime;

    public Booking(String userId, int bikeId, long bookingTime) {
        this.userId = userId;
        this.bikeId = bikeId;
        this.bookingTime = bookingTime;
    }

    // --- Getters ---
    public String getUserId() { return userId; }
    public int getBikeId() { return bikeId; }
    public long getBookingTime() { return bookingTime; }
}