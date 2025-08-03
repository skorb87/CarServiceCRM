package me.skorb.entity;

import java.time.LocalDateTime;

public class Appointment {

    private int id;

    private Customer customer;

    private Vehicle vehicle;

    private LocalDateTime dateTime;

    private Status status = Status.Scheduled;

    private String notes;

    public enum Status {
        Scheduled, Canceled, Completed
    }

    // Constructors
    public Appointment() {}

    public Appointment(Customer customer, Vehicle vehicle, LocalDateTime dateTime, Status status, String notes) {
        this.customer = customer;
        this.vehicle = vehicle;
        this.dateTime = dateTime;
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

}
