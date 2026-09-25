package com.dealerlink.model;

public class Order {
    private int id;
    private int quotationId;
    private int requestId;
    private int shopId;
    private String shopName;   // joined
    private int dealerId;
    private String dealerName; // joined
    private String productName; // joined
    private double totalAmount;
    private String status; // CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    private String createdAt;

    // Delivery fields (joined from deliveries table for convenience)
    private String deliveryStatus;
    private String deliveryLocation;
    private String weatherNote;
    private String estimatedArrival;

    public Order() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuotationId() { return quotationId; }
    public void setQuotationId(int quotationId) { this.quotationId = quotationId; }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public int getDealerId() { return dealerId; }
    public void setDealerId(int dealerId) { this.dealerId = dealerId; }

    public String getDealerName() { return dealerName; }
    public void setDealerName(String dealerName) { this.dealerName = dealerName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public String getDeliveryLocation() { return deliveryLocation; }
    public void setDeliveryLocation(String deliveryLocation) { this.deliveryLocation = deliveryLocation; }

    public String getWeatherNote() { return weatherNote; }
    public void setWeatherNote(String weatherNote) { this.weatherNote = weatherNote; }

    public String getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(String estimatedArrival) { this.estimatedArrival = estimatedArrival; }
}