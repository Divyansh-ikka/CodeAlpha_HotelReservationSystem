package com.hotel.service;

import com.hotel.model.Reservation;
import com.hotel.model.Room;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class HotelService {
    private List<Room> rooms;
    private Map<String, Reservation> reservations;
    private static final String DATA_FILE = "reservations.ser";

    public HotelService() {
        loadData(); // Load rooms and reservations first
        initializeRooms(); // Only add default rooms if the list is empty
    }

    private void initializeRooms() {
        if (rooms.isEmpty()) {
            // Standard rooms
            for (int i = 1; i <= 10; i++) {
                rooms.add(new Room(100 + i, Room.RoomType.STANDARD, 99.99, 2));
            }
            // Deluxe rooms
            for (int i = 1; i <= 5; i++) {
                rooms.add(new Room(200 + i, Room.RoomType.DELUXE, 159.99, 4));
            }
            // Suites
            for (int i = 1; i <= 3; i++) {
                rooms.add(new Room(300 + i, Room.RoomType.SUITE, 249.99, 6));
            }
        }
    }

    public List<Room> findAvailableRooms(Room.RoomType type, LocalDate checkIn, LocalDate checkOut) {
        // Find all rooms that are booked during the requested date range
        Set<Integer> bookedRoomNumbers = reservations.values().stream()
                .filter(res -> !res.isCancelled() && dateRangesOverlap(checkIn, checkOut, res.getCheckInDate(), res.getCheckOutDate()))
                .map(res -> res.getRoom().getRoomNumber())
                .collect(Collectors.toSet());

        // Return all rooms that are not in the booked list and match the type
        return rooms.stream()
                .filter(room -> !bookedRoomNumbers.contains(room.getRoomNumber()))
                .filter(room -> type == null || room.getType() == type)
                .collect(Collectors.toList());
    }

    private boolean dateRangesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        // An overlap occurs if one range starts before the other ends, and vice-versa.
        // The ranges are [start, end). The checkout day is not included in the stay.
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public Reservation makeReservation(Room room, String guestName, String guestEmail,
                                     LocalDate checkIn, LocalDate checkOut) {
        // Final check to prevent double booking
        if (!isRoomAvailable(room, checkIn, checkOut)) {
            throw new IllegalStateException("Room is not available for the selected dates.");
        }

        Reservation reservation = new Reservation(room, guestName, guestEmail, checkIn, checkOut);
        reservations.put(reservation.getReservationId(), reservation);
        saveData();
        return reservation;
    }

    private boolean isRoomAvailable(Room room, LocalDate checkIn, LocalDate checkOut) {
        return reservations.values().stream()
                .noneMatch(res -> !res.isCancelled() &&
                        res.getRoom().getRoomNumber() == room.getRoomNumber() &&
                        dateRangesOverlap(checkIn, checkOut, res.getCheckInDate(), res.getCheckOutDate()));
    }

    public void cancelReservation(String reservationId) {
        Reservation reservation = reservations.get(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found: " + reservationId);
        }
        if (reservation.isCancelled()) {
            throw new IllegalStateException("Reservation is already cancelled.");
        }
        reservation.cancel();
        saveData();
    }

    public Reservation findReservation(String reservationId) {
        return reservations.get(reservationId);
    }

    public void processPayment(String reservationId) {
        Reservation reservation = findReservation(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found: " + reservationId);
        }
        if (reservation.isPaid()) {
            throw new IllegalStateException("Payment has already been processed.");
        }
        if (reservation.isCancelled()) {
            throw new IllegalStateException("Cannot process payment for a cancelled reservation.");
        }
        reservation.processPayment();
        saveData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        this.rooms = new ArrayList<>();
        this.reservations = new HashMap<>();
        File dataFile = new File(DATA_FILE);
        if (!dataFile.exists()) {
            System.out.println("No existing data found. Starting with a fresh system.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            this.rooms = (List<Room>) ois.readObject();
            this.reservations = (Map<String, Reservation>) ois.readObject();
            System.out.println("Data loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data: " + e.getMessage());
            // If data is corrupt, start fresh
            this.rooms = new ArrayList<>();
            this.reservations = new HashMap<>();
        }
    }

    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(rooms);
            oos.writeObject(reservations);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
}
