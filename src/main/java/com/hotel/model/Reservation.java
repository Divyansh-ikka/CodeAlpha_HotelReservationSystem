package com.hotel.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Reservation implements Serializable {
    private String reservationId;
    private Room room;
    private String guestName;
    private String guestEmail;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double totalPrice;
    private boolean isPaid;
    private boolean isCancelled;

    public Reservation(Room room, String guestName, String guestEmail, 
                      LocalDate checkInDate, LocalDate checkOutDate) {
        this.reservationId = UUID.randomUUID().toString().substring(0, 8);
        this.room = room;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = calculateTotalPrice();
        this.isPaid = false;
        this.isCancelled = false;
        
    }

    private double calculateTotalPrice() {
        long numberOfNights = java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        return numberOfNights * room.getPricePerNight();
    }

    // Getters
    public String getReservationId() {
        return reservationId;
    }

    public Room getRoom() {
        return room;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    // Business methods
    public void processPayment() {
        this.isPaid = true;
    }

    public void cancel() {
        this.isCancelled = true;
    }

    @Override
    public String toString() {
        return String.format("""
            Reservation ID: %s
            Guest: %s (%s)
            Room: %d - %s
            Check-in: %s
            Check-out: %s
            Total Price: $%.2f
            Status: %s%s""",
            reservationId, guestName, guestEmail,
            room.getRoomNumber(), room.getType(),
            checkInDate, checkOutDate,
            totalPrice,
            isCancelled ? "CANCELLED" : (isPaid ? "PAID" : "PENDING PAYMENT"),
            isCancelled ? "" : String.format("\n            Payment %s", isPaid ? "Received" : "Pending")
        );
    }
}
