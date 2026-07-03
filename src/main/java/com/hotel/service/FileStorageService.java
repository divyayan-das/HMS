package com.hotel.service;

import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/* FileStorageService - handles permanent storage of all data in flat files.*/
public class FileStorageService {

    private static final String DATA_DIR = "hotel_data";
    private static final String ROOMS_FILE = DATA_DIR + "/rooms.csv";
    private static final String CUSTOMERS_FILE = DATA_DIR + "/customers.csv";
    private static final String BOOKINGS_FILE = DATA_DIR + "/bookings.csv";

    public FileStorageService() {
        initDataDirectory();
    }

    private void initDataDirectory() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Could not create data directory: " + e.getMessage());
        }
    }

    // ROOMS

    public void saveRooms(List<Room> rooms) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ROOMS_FILE))) {
            writer.write("roomNumber,roomType,pricePerDay,status");
            writer.newLine();
            for (Room room : rooms) {
                writer.write(room.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving rooms: " + e.getMessage());
        }
    }

    public List<Room> loadRooms() {
        List<Room> rooms = new ArrayList<>();
        File file = new File(ROOMS_FILE);
        if (!file.exists()) return rooms;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // skip header
                line = line.trim();
                if (!line.isEmpty()) {
                    try { rooms.add(Room.fromFileString(line)); }
                    catch (Exception e) { System.err.println("Skipping bad room line: " + line); }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading rooms: " + e.getMessage());
        }
        return rooms;
    }

    // CUSTOMERS

    public void saveCustomers(List<Customer> customers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CUSTOMERS_FILE))) {
            writer.write("customerId,name,contactNumber,email");
            writer.newLine();
            for (Customer c : customers) {
                writer.write(c.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving customers: " + e.getMessage());
        }
    }

    public List<Customer> loadCustomers() {
        List<Customer> customers = new ArrayList<>();
        File file = new File(CUSTOMERS_FILE);
        if (!file.exists()) return customers;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                line = line.trim();
                if (!line.isEmpty()) {
                    try { customers.add(Customer.fromFileString(line)); }
                    catch (Exception e) { System.err.println("Skipping bad customer line: " + line); }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading customers: " + e.getMessage());
        }
        return customers;
    }

    // BOOKINGS

    public void saveBookings(List<Booking> bookings) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKINGS_FILE))) {
            writer.write("bookingId,customerId,roomNumber,checkIn,checkOut,total,status,customerName,pricePerDay");
            writer.newLine();
            for (Booking b : bookings) {
                writer.write(b.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving bookings: " + e.getMessage());
        }
    }

    public List<Booking> loadBookings() {
        List<Booking> bookings = new ArrayList<>();
        File file = new File(BOOKINGS_FILE);
        if (!file.exists()) return bookings;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                line = line.trim();
                if (!line.isEmpty()) {
                    try { bookings.add(Booking.fromFileString(line)); }
                    catch (Exception e) { System.err.println("Skipping bad booking line: " + line); }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading bookings: " + e.getMessage());
        }
        return bookings;
    }
}
