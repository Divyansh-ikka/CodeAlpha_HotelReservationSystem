package com.hotel.service;

import com.hotel.model.Room;
import com.hotel.model.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HotelServiceTest {
    private HotelService hotelService;
    @BeforeEach
    void setUp() {
        // Use a different data file for testing
        hotelService = new HotelService();
        // Clear any existing data
        hotelService = new HotelService();
    }

    @Test
    void testFindAvailableRooms() {
        // Test finding available standard rooms
        List<Room> availableRooms = hotelService.findAvailableRooms(
            Room.RoomType.STANDARD, 
            LocalDate.now().plusDays(1), 
            LocalDate.now().plusDays(3)
        );
        
        assertNotNull(availableRooms);
        assertFalse(availableRooms.isEmpty());
        assertTrue(availableRooms.stream().allMatch(room -> 
            room.getType() == Room.RoomType.STANDARD && room.isAvailable()
        ));
    }

    @Test
    void testMakeAndFindReservation() {
        // Get an available room
        List<Room> availableRooms = hotelService.findAvailableRooms(
            Room.RoomType.STANDARD,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2)
        );
        
        assertFalse(availableRooms.isEmpty());
        Room room = availableRooms.get(0);
        
        // Make a reservation
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);
        Reservation reservation = hotelService.makeReservation(
            room, 
            "John Doe", 
            "john@example.com",
            checkIn,
            checkOut
        );
        
        assertNotNull(reservation);
        assertEquals("John Doe", reservation.getGuestName());
        assertEquals(room, reservation.getRoom());
        assertFalse(room.isAvailable());
        
        // Find the reservation
        Reservation foundReservation = hotelService.findReservation(reservation.getReservationId());
        assertNotNull(foundReservation);
        assertEquals(reservation.getReservationId(), foundReservation.getReservationId());
    }

    @Test
    void testCancelReservation() {
        // Make a reservation first
        List<Room> availableRooms = hotelService.findAvailableRooms(
            Room.RoomType.DELUXE,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2)
        );
        
        assertFalse(availableRooms.isEmpty());
        Room room = availableRooms.get(0);
        
        Reservation reservation = hotelService.makeReservation(
            room,
            "Jane Smith",
            "jane@example.com",
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(3)
        );
        
        // Test canceling the reservation
        hotelService.cancelReservation(reservation.getReservationId());
        Reservation cancelledReservation = hotelService.findReservation(reservation.getReservationId());
        
        assertTrue(cancelledReservation.isCancelled());
        assertTrue(room.isAvailable());
    }

    @Test
    void testProcessPayment() {
        // Make a reservation
        List<Room> availableRooms = hotelService.findAvailableRooms(
            Room.RoomType.SUITE,
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(4)
        );
        
        assertFalse(availableRooms.isEmpty());
        Room room = availableRooms.get(0);
        
        Reservation reservation = hotelService.makeReservation(
            room,
            "Bob Johnson",
            "bob@example.com",
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(4)
        );
        
        // Process payment
        hotelService.processPayment(reservation.getReservationId());
        Reservation paidReservation = hotelService.findReservation(reservation.getReservationId());
        
        assertTrue(paidReservation.isPaid());
    }

    @Test
    void testRoomUniqueness() {
        // Test that the same room can't be double-booked
        List<Room> availableRooms = hotelService.findAvailableRooms(
            null, // any type
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2)
        );
        
        assertFalse(availableRooms.isEmpty());
        Room room = availableRooms.get(0);
        
        // First reservation should succeed
        hotelService.makeReservation(
            room,
            "First Guest",
            "first@example.com",
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2)
        );
        
        // Second reservation for the same room should fail
        assertThrows(IllegalStateException.class, () -> {
            hotelService.makeReservation(
                room,
                "Second Guest",
                "second@example.com",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2)
            );
        });
    }
}
