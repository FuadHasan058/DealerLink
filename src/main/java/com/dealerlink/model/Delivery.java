package com.dealerlink.model;

public class Delivery {
    private int id;
    private int orderId;
    private String currentStatus; // PREPARING, SHIPPED, DELIVERED
    private String currentLocation;
    private String weatherNote;
    private String estimatedArrival;
    private String updatedAt;

    public Delivery() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }

    public String getWeatherNote() { return weatherNote; }
    public void setWeatherNote(String weatherNote) { this.weatherNote = weatherNote; }

    public String getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(String estimatedArrival) { this.estimatedArrival = estimatedArrival; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}