package com.dealerlink.dao;

import com.dealerlink.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

public class DeliveryDAO {

    public void createForOrder(int orderId) throws java.sql.SQLException {
        String sql = "INSERT INTO deliveries(order_id, current_status, updated_at) VALUES (?, 'PREPARING', ?)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setString(2, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    public void updateStatusAndLocation(int orderId, String status, String location, String weatherNote) throws java.sql.SQLException {
        String sql = "UPDATE deliveries SET current_status = ?, current_location = ?, weather_note = ?, updated_at = ? " +
                "WHERE order_id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, location);
            ps.setString(3, weatherNote);
            ps.setString(4, LocalDateTime.now().toString());
            ps.setInt(5, orderId);
            ps.executeUpdate();
        }
    }

    public void updateEstimatedArrival(int orderId, String estimatedArrival) throws java.sql.SQLException {
        String sql = "UPDATE deliveries SET estimated_arrival = ?, updated_at = ? WHERE order_id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estimatedArrival);
            ps.setString(2, LocalDateTime.now().toString());
            ps.setInt(3, orderId);
            ps.executeUpdate();
        }
    }
}