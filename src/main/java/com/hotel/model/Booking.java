package com.hotel.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/* Booking model representing a room reservation.*/
public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum BookingStatus { ACTIVE, CHECKED_OUT, CANCELLED }

    private int bookingId;
    private int customerId;
    private int roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double totalAmount;
    private BookingStatus status;
    private String customerName;
    private double roomPricePerDay;

    public Booking() {}

    public Booking(int bookingId, int customerId, int roomNumber,
                   LocalDate checkInDate, LocalDate checkOutDate,
                   double roomPricePerDay, String customerName) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.roomPricePerDay = roomPricePerDay;
        this.customerName = customerName;
        this.status = BookingStatus.ACTIVE;
        calculateTotal();
    }

    public void calculateTotal() {
        if (checkInDate != null && checkOutDate != null) {
            long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            if (days <= 0) days = 1;
            this.totalAmount = days * roomPricePerDay;
        }
    }

    public long getNumberOfDays() {
        if (checkInDate != null && checkOutDate != null) {
            long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            return days <= 0 ? 1 : days;
        }
        return 1;
    }

    // Getters and Setters
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
        calculateTotal();
    }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
        calculateTotal();
    }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public double getRoomPricePerDay() { return roomPricePerDay; }
    public void setRoomPricePerDay(double roomPricePerDay) {
        this.roomPricePerDay = roomPricePerDay;
        calculateTotal();
    }

    public String toFileString() {
        return bookingId + "," + customerId + "," + roomNumber + "," +
               checkInDate + "," + checkOutDate + "," +
               totalAmount + "," + status.name() + "," +
               customerName + "," + roomPricePerDay;
    }

    public static Booking fromFileString(String line) {
        String[] parts = line.split(",", 9);
        Booking b = new Booking();
        b.setBookingId(Integer.parseInt(parts[0].trim()));
        b.setCustomerId(Integer.parseInt(parts[1].trim()));
        b.setRoomNumber(Integer.parseInt(parts[2].trim()));
        b.setCheckInDate(LocalDate.parse(parts[3].trim()));
        b.setCheckOutDate(LocalDate.parse(parts[4].trim()));
        b.setTotalAmount(Double.parseDouble(parts[5].trim()));
        b.setStatus(BookingStatus.valueOf(parts[6].trim()));
        b.setCustomerName(parts[7].trim());
        b.setRoomPricePerDay(Double.parseDouble(parts[8].trim()));
        return b;
    }
}
