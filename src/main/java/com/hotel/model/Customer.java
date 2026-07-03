package com.hotel.model;

import java.io.Serializable;

/* Customer model holding guest information.*/
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    private int customerId;
    private String name;
    private String contactNumber;
    private String email;

    public Customer() {}

    public Customer(int customerId, String name, String contactNumber, String email) {
        this.customerId = customerId;
        this.name = name;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    // Getters and Setters
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String toFileString() {
        return customerId + "," + name + "," + contactNumber + "," + email;
    }

    public static Customer fromFileString(String line) {
        String[] parts = line.split(",", 4);
        Customer c = new Customer();
        c.setCustomerId(Integer.parseInt(parts[0].trim()));
        c.setName(parts[1].trim());
        c.setContactNumber(parts[2].trim());
        c.setEmail(parts[3].trim());
        return c;
    }

    @Override
    public String toString() {
        return "[" + customerId + "] " + name + " | " + contactNumber;
    }
}
