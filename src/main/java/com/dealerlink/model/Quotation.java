package com.dealerlink.model;

public class Quotation {
    private int id;
    private int requestId;
    private int dealerId;
    private String dealerName; // joined
    private String dealerCity; // joined
    private double pricePerUnit;
    private double totalPrice;
    private int estimatedDeliveryDays;
    private String message;
    private String status; // PENDING, ACCEPTED, REJECTED
    private String createdAt;
    private String weatherNote; // transient, filled in via WeatherService at runtime (not persisted)

    public Quotation() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getDealerId() { return dealerId; }
    public void setDealerId(int dealerId) { this.dealerId = dealerId; }

    public String getDealerName() { return dealerName; }
    public void setDealerName(String dealerName) { this.dealerName = dealerName; }

    public String getDealerCity() { return dealerCity; }
    public void setDealerCity(String dealerCity) { this.dealerCity = dealerCity; }

    public double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public int getEstimatedDeliveryDays() { return estimatedDeliveryDays; }
    public void setEstimatedDeliveryDays(int estimatedDeliveryDays) { this.estimatedDeliveryDays = estimatedDeliveryDays; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getWeatherNote() { return weatherNote; }
    public void setWeatherNote(String weatherNote) { this.weatherNote = weatherNote; }
}