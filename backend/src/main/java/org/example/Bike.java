package org.example;

public class Bike {
    private int id;
    private String model;
    private String location;
    private boolean isAvailable;
    private String bookedBy;
    private String owner;

    // --- ADDED NEW FIELDS ---
    private int modelYear;
    private double rentRate;
    private String contactNumber;
    private String photoUrl;

    // --- UPDATED CONSTRUCTOR ---
    public Bike(int id, String model, String location, String owner,
                int modelYear, double rentRate, String contactNumber, String photoUrl) {
        this.id = id;
        this.model = model;
        this.location = location;
        this.owner = owner;
        this.isAvailable = true;
        this.bookedBy = null;

        this.modelYear = modelYear;
        this.rentRate = rentRate;
        this.contactNumber = contactNumber;
        this.photoUrl = photoUrl;
    }

    // --- GETTERS ---
    // (Existing getters for id, model, location, isAvailable, bookedBy, owner)

    public int getId() { return id; }
    public String getModel() { return model; }
    public String getLocation() { return location; }
    public boolean isAvailable() { return isAvailable; }
    public String getBookedBy() { return bookedBy; }
    public String getOwner() { return owner; }

    // --- ADD NEW GETTERS ---
    public int getModelYear() { return modelYear; }
    public double getRentRate() { return rentRate; }
    public String getContactNumber() { return contactNumber; }
    public String getPhotoUrl() { return photoUrl; }

    // --- SETTERS ---
    // (Existing setters for isAvailable, bookedBy)
    public void setAvailable(boolean available) { isAvailable = available; }
    public void setBookedBy(String bookedBy) { this.bookedBy = bookedBy; }
}