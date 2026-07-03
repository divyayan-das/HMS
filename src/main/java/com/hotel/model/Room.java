package com.hotel.model;

import java.io.Serializable;

/* Room model representing a hotel room with all its attributes.*/
public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum RoomType { SINGLE, DOUBLE, DELUXE }
    public enum RoomStatus { AVAILABLE, OCCUPIED }

    private int roomNumber;
    private RoomType roomType;
    private double pricePerDay;
    private RoomStatus status;

    public Room() {}

    public Room(int roomNumber, RoomType roomType, double pricePerDay) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerDay = pricePerDay;
        this.status = RoomStatus.AVAILABLE;
    }

    // Getters and Setters
    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    public boolean isAvailable() { return status == RoomStatus.AVAILABLE; }

    public String toFileString() {
        return roomNumber + "," + roomType.name() + "," + pricePerDay + "," + status.name();
    }

    public static Room fromFileString(String line) {
        String[] parts = line.split(",");
        Room room = new Room();
        room.setRoomNumber(Integer.parseInt(parts[0].trim()));
        room.setRoomType(RoomType.valueOf(parts[1].trim()));
        room.setPricePerDay(Double.parseDouble(parts[2].trim()));
        room.setStatus(RoomStatus.valueOf(parts[3].trim()));
        return room;
    }

    @Override
    public String toString() {
        return "Room #" + roomNumber + " [" + roomType + "] - ₹" + pricePerDay + "/day - " + status;
    }
}
