package com.dealerlink.model;

public class ProductRequest {
    private int id;
    private int shopId;
    private String shopName;   // joined
    private String shopCity;   // joined
    private int productId;
    private String productName; // joined
    private String unit;        // joined
    private int quantity;
    private String notes;
    private String status; // OPEN, QUOTED, ORDERED, CLOSED
    private String createdAt;

    public ProductRequest() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getShopCity() { return shopCity; }
    public void setShopCity(String shopCity) { this.shopCity = shopCity; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}