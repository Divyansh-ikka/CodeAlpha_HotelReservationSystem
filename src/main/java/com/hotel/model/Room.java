package com.hotel.model;

import java.io.Serializable;

public class Room implements Serializable {
    private int roomNumber;
    private RoomType type;
    private double pricePerNight;
    private boolean isAvailable;
    private int maxOccupancy;

    public enum RoomType {
        STANDARD,
        DELUXE,
        SUITE
    }

    public Room(int roomNumber, RoomType type, double pricePerNight, int maxOccupancy) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.maxOccupancy = maxOccupancy;
        this.isAvailable = true;
    }

    // Getters and Setters
    public int getRoomNumber() {
        return roomNumber;
    }

    public RoomType getType() {
        return type;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }

    @Override
    public String toString() {
        return String.format("Room %d - %s (Max: %d people) - $%.2f/night - %s",
                roomNumber, type, maxOccupancy, pricePerNight, 
                isAvailable ? "Available" : "Booked");
    }
}
