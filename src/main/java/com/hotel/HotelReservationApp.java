package com.hotel;

import com.hotel.model.Room;
import com.hotel.model.Reservation;
import com.hotel.service.HotelService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class HotelReservationApp {
    private static final HotelService hotelService = new HotelService();
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        System.out.println("=== Welcome to the Hotel Reservation System ===\n");
        
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = getIntInput("Enter your choice: ", 1, 5);
            
            switch (choice) {
                case 1 -> searchAndBookRoom();
                case 2 -> viewReservation();
                case 3 -> cancelReservation();
                case 4 -> processPayment();
                case 5 -> {
                    System.out.println("Thank you for using the Hotel Reservation System. Goodbye!");
                    running = false;
                }
            }
        }
        
        scanner.close();
    }

    private static void printMainMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Search & Book a Room");
        System.out.println("2. View Reservation Details");
        System.out.println("3. Cancel Reservation");
        System.out.println("4. Process Payment");
        System.out.println("5. Exit");
    }

    private static void searchAndBookRoom() {
        System.out.println("\n=== Search & Book a Room ===");
        
        // Get room type
        System.out.println("\nAvailable Room Types:");
        System.out.println("1. Standard");
        System.out.println("2. Deluxe");
        System.out.println("3. Suite");
        System.out.println("4. Any Type");
        int typeChoice = getIntInput("Select room type (1-4): ", 1, 4);
        
        Room.RoomType roomType = switch (typeChoice) {
            case 1 -> Room.RoomType.STANDARD;
            case 2 -> Room.RoomType.DELUXE;
            case 3 -> Room.RoomType.SUITE;
            default -> null;
        };
        
        // Get dates
        LocalDate checkIn = getDateInput("Enter check-in date (YYYY-MM-DD): ");
        LocalDate checkOut = getDateInput("Enter check-out date (YYYY-MM-DD): ");
        
        if (checkOut.isBefore(checkIn.plusDays(1))) {
            System.out.println("Error: Check-out date must be after check-in date.");
            return;
        }
        
        // Find available rooms
        List<Room> availableRooms = hotelService.findAvailableRooms(roomType, checkIn, checkOut);
        
        if (availableRooms.isEmpty()) {
            System.out.println("\nNo rooms available for the selected criteria.");
            return;
        }
        
        // Display available rooms
        System.out.println("\nAvailable Rooms:");
        for (int i = 0; i < availableRooms.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, availableRooms.get(i));
        }
        
        // Select a room
        int roomChoice = getIntInput("\nSelect a room number to book (0 to cancel): ", 
                                   0, availableRooms.size());
        if (roomChoice == 0) {
            System.out.println("Booking cancelled.");
            return;
        }
        
        Room selectedRoom = availableRooms.get(roomChoice - 1);
        
        // Get guest information
        System.out.print("\nEnter your full name: ");
        String guestName = scanner.nextLine();
        
        String guestEmail;
        do {
            System.out.print("Enter your email: ");
            guestEmail = scanner.nextLine().trim();
            if (!guestEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                System.out.println("Invalid email format. Please try again.");
            }
        } while (!guestEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$"));
        
        // Confirm booking
        System.out.println("\n=== Booking Summary ===");
        System.out.println("Room: " + selectedRoom);
        System.out.println("Check-in: " + checkIn);
        System.out.println("Check-out: " + checkOut);
        System.out.println("Guest: " + guestName + " (" + guestEmail + ")");
        
        double totalPrice = selectedRoom.getPricePerNight() * 
                           java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        System.out.printf("Total Price: $%.2f%n", totalPrice);
        
        System.out.print("\nConfirm booking? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if (confirm.equals("yes") || confirm.equals("y")) {
            try {
                Reservation reservation = hotelService.makeReservation(
                    selectedRoom, guestName, guestEmail, checkIn, checkOut);
                System.out.println("\n=== Booking Confirmed! ===");
                System.out.println("Your reservation ID is: " + reservation.getReservationId());
                System.out.println("Please keep this ID for future reference.");
            } catch (Exception e) {
                System.out.println("Error creating reservation: " + e.getMessage());
            }
        } else {
            System.out.println("Booking cancelled.");
        }
    }

    private static void viewReservation() {
        System.out.println("\n=== View Reservation ===");
        String reservationId = getInput("Enter your reservation ID: ");
        
        try {
            Reservation reservation = hotelService.findReservation(reservationId);
            if (reservation == null) {
                System.out.println("No reservation found with ID: " + reservationId);
                return;
            }
            
            System.out.println("\n=== Reservation Details ===");
            System.out.println(reservation);
        } catch (Exception e) {
            System.out.println("Error retrieving reservation: " + e.getMessage());
        }
    }

    private static void cancelReservation() {
        System.out.println("\n=== Cancel Reservation ===");
        String reservationId = getInput("Enter your reservation ID to cancel: ");
        
        try {
            hotelService.cancelReservation(reservationId);
            System.out.println("Reservation " + reservationId + " has been cancelled successfully.");
        } catch (Exception e) {
            System.out.println("Error cancelling reservation: " + e.getMessage());
        }
    }

    private static void processPayment() {
        System.out.println("\n=== Process Payment ===");
        String reservationId = getInput("Enter reservation ID for payment: ");
        
        try {
            hotelService.processPayment(reservationId);
            System.out.println("Payment processed successfully for reservation " + reservationId);
        } catch (Exception e) {
            System.out.println("Error processing payment: " + e.getMessage());
        }
    }

    // Utility methods
    private static String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int getIntInput(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private static LocalDate getDateInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String dateStr = scanner.nextLine().trim();
                return LocalDate.parse(dateStr, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD format.");
            }
        }
    }
}
