package com.hotel.service;

import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/*HotelService - Central business logic for the Hotel Management System.*/
public class HotelService {

    private final FileStorageService fileStorage;

    private List<Room> rooms;
    private List<Customer> customers;
    private List<Booking> bookings;

    private int nextCustomerId = 1;
    private int nextBookingId = 1;

    public HotelService() {
        this.fileStorage = new FileStorageService();
        loadAllData();
        seedDefaultRoomsIfEmpty();
    }

    private void loadAllData() {
        rooms = fileStorage.loadRooms();
        customers = fileStorage.loadCustomers();
        bookings = fileStorage.loadBookings();

        // Recalculate next IDs
        customers.stream().mapToInt(Customer::getCustomerId).max()
                 .ifPresent(max -> nextCustomerId = max + 1);
        bookings.stream().mapToInt(Booking::getBookingId).max()
                .ifPresent(max -> nextBookingId = max + 1);
    }

    private void seedDefaultRoomsIfEmpty() {
        if (rooms.isEmpty()) {
            rooms.add(new Room(101, Room.RoomType.SINGLE,  80.0));
            rooms.add(new Room(102, Room.RoomType.SINGLE,  80.0));
            rooms.add(new Room(103, Room.RoomType.DOUBLE, 120.0));
            rooms.add(new Room(104, Room.RoomType.DOUBLE, 120.0));
            rooms.add(new Room(201, Room.RoomType.DELUXE, 200.0));
            rooms.add(new Room(202, Room.RoomType.DELUXE, 200.0));
            rooms.add(new Room(203, Room.RoomType.SINGLE,  85.0));
            rooms.add(new Room(204, Room.RoomType.DOUBLE, 130.0));
            saveRooms();
        }
    }

    // ROOM OPERATIONS 

    public List<Room> getAllRooms() { return new ArrayList<>(rooms); }

    public List<Room> getAvailableRooms() {
        return rooms.stream().filter(Room::isAvailable).collect(Collectors.toList());
    }

    public Optional<Room> findRoom(int roomNumber) {
        return rooms.stream().filter(r -> r.getRoomNumber() == roomNumber).findFirst();
    }

    public boolean addRoom(Room room) {
        boolean exists = rooms.stream().anyMatch(r -> r.getRoomNumber() == room.getRoomNumber());
        if (exists) return false;
        rooms.add(room);
        saveRooms();
        return true;
    }

    public boolean deleteRoom(int roomNumber) {
        Optional<Room> room = findRoom(roomNumber);
        if (room.isEmpty()) return false;
        if (!room.get().isAvailable()) return false; // can't delete occupied room
        rooms.removeIf(r -> r.getRoomNumber() == roomNumber);
        saveRooms();
        return true;
    }

    // CUSTOMER OPERATIONS 

    public List<Customer> getAllCustomers() { return new ArrayList<>(customers); }

    public Optional<Customer> findCustomer(int customerId) {
        return customers.stream().filter(c -> c.getCustomerId() == customerId).findFirst();
    }

    public Customer addCustomer(String name, String contactNumber, String email) {
        Customer customer = new Customer(nextCustomerId++, name, contactNumber, email);
        customers.add(customer);
        saveCustomers();
        return customer;
    }

    public boolean deleteCustomer(int customerId) {
        boolean hasActiveBooking = bookings.stream()
            .anyMatch(b -> b.getCustomerId() == customerId && b.getStatus() == Booking.BookingStatus.ACTIVE);
        if (hasActiveBooking) return false;
        customers.removeIf(c -> c.getCustomerId() == customerId);
        saveCustomers();
        return true;
    }

    // BOOKING OPERATIONS 

    public List<Booking> getAllBookings() { return new ArrayList<>(bookings); }

    public List<Booking> getActiveBookings() {
        return bookings.stream()
                       .filter(b -> b.getStatus() == Booking.BookingStatus.ACTIVE)
                       .collect(Collectors.toList());
    }

    /* Book a room. Returns the created Booking, or null if room is unavailable.*/
    public Booking bookRoom(int customerId, int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        Optional<Room> roomOpt = findRoom(roomNumber);
        Optional<Customer> customerOpt = findCustomer(customerId);

        if (roomOpt.isEmpty() || customerOpt.isEmpty()) return null;
        Room room = roomOpt.get();
        Customer customer = customerOpt.get();
        if (!room.isAvailable()) return null;

        Booking booking = new Booking(
            nextBookingId++, customerId, roomNumber,
            checkIn, checkOut, room.getPricePerDay(), customer.getName()
        );
        bookings.add(booking);
        room.setStatus(Room.RoomStatus.OCCUPIED);
        saveAll();
        return booking;
    }

    /* Checkout a booking. Releases the room and marks booking as checked out.*/
    public Booking checkout(int bookingId) {
        Optional<Booking> bookingOpt = bookings.stream()
            .filter(b -> b.getBookingId() == bookingId && b.getStatus() == Booking.BookingStatus.ACTIVE)
            .findFirst();

        if (bookingOpt.isEmpty()) return null;
        Booking booking = bookingOpt.get();
        booking.setStatus(Booking.BookingStatus.CHECKED_OUT);

        findRoom(booking.getRoomNumber()).ifPresent(r -> r.setStatus(Room.RoomStatus.AVAILABLE));
        saveAll();
        return booking;
    }

    public boolean cancelBooking(int bookingId) {
        Optional<Booking> bookingOpt = bookings.stream()
            .filter(b -> b.getBookingId() == bookingId && b.getStatus() == Booking.BookingStatus.ACTIVE)
            .findFirst();
        if (bookingOpt.isEmpty()) return false;
        Booking booking = bookingOpt.get();
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        findRoom(booking.getRoomNumber()).ifPresent(r -> r.setStatus(Room.RoomStatus.AVAILABLE));
        saveAll();
        return true;
    }

    // BILLING 

    /*Generate bill string for a booking. (Extra Feature #4 - Billing Management)*/
    public String generateBill(int bookingId) {
        Optional<Booking> opt = bookings.stream()
            .filter(b -> b.getBookingId() == bookingId).findFirst();
        if (opt.isEmpty()) return null;
        Booking b = opt.get();
        double tax = b.getTotalAmount() * 0.10;
        double grandTotal = b.getTotalAmount() + tax;

        return String.format(
            "╔══════════════════════════════════════╗\n" +
            "║       HOTEL MANAGEMENT SYSTEM        ║\n" +
            "║             INVOICE                  ║\n" +
            "╠══════════════════════════════════════╣\n" +
            "║  Booking ID  : %-22d║\n" +
            "║  Customer    : %-22s║\n" +
            "║  Room No.    : %-22d║\n" +
            "║  Check-In    : %-22s║\n" +
            "║  Check-Out   : %-22s║\n" +
            "║  Nights      : %-22d║\n" +
            "╠══════════════════════════════════════╣\n" +
            "║  Rate/Night  : ₹%-21.2f║\n" +
            "║  Subtotal    : ₹%-21.2f║\n" +
            "║  Tax (10%%)   : ₹%-21.2f║\n" +
            "║  TOTAL       : ₹%-21.2f║\n" +
            "╠══════════════════════════════════════╣\n" +
            "║  Status      : %-22s║\n" +
            "╚══════════════════════════════════════╝",
            b.getBookingId(), b.getCustomerName(),
            b.getRoomNumber(),
            b.getCheckInDate(), b.getCheckOutDate(),
            b.getNumberOfDays(),
            b.getRoomPricePerDay(), b.getTotalAmount(),
            tax, grandTotal,
            b.getStatus().name()
        );
    }

    // Revenue stats for dashboard
    public double getTotalRevenue() {
        return bookings.stream()
            .filter(b -> b.getStatus() == Booking.BookingStatus.CHECKED_OUT)
            .mapToDouble(Booking::getTotalAmount).sum();
    }

    public long getOccupiedRoomsCount() {
        return rooms.stream().filter(r -> !r.isAvailable()).count();
    }

    public long getTotalBookingsCount() { return bookings.size(); }

    // PERSISTENCE 

    private void saveRooms()     { fileStorage.saveRooms(rooms); }
    private void saveCustomers() { fileStorage.saveCustomers(customers); }
    private void saveBookings()  { fileStorage.saveBookings(bookings); }

    public void saveAll() {
        saveRooms();
        saveCustomers();
        saveBookings();
    }
}
