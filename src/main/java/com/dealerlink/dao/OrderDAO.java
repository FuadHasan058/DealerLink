package com.dealerlink.dao;

import com.dealerlink.db.DatabaseManager;
import com.dealerlink.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    private static final String JOIN_SELECT =
            "SELECT o.*, su.name AS shop_name, du.name AS dealer_name, p.name AS product_name, " +
                    "d.current_status AS delivery_status, d.current_location AS delivery_location, " +
                    "d.weather_note AS weather_note, d.estimated_arrival AS estimated_arrival " +
                    "FROM orders o " +
                    "JOIN users su ON o.shop_id = su.id " +
                    "JOIN users du ON o.dealer_id = du.id " +
                    "JOIN requests r ON o.request_id = r.id " +
                    "JOIN products p ON r.product_id = p.id " +
                    "LEFT JOIN deliveries d ON d.order_id = o.id ";

    public int createOrder(int quotationId, int requestId, int shopId, int dealerId, double totalAmount) throws SQLException {
        String sql = "INSERT INTO orders(quotation_id, request_id, shop_id, dealer_id, total_amount, status, created_at) " +
                "VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, quotationId);
            ps.setInt(2, requestId);
            ps.setInt(3, shopId);
            ps.setInt(4, dealerId);
            ps.setDouble(5, totalAmount);
            ps.setString(6, "CONFIRMED");
            ps.setString(7, LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public List<Order> getOrdersByShop(int shopId) throws SQLException {
        String sql = JOIN_SELECT + "WHERE o.shop_id = ? ORDER BY o.created_at DESC";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    public List<Order> getOrdersByDealer(int dealerId) throws SQLException {
        String sql = JOIN_SELECT + "WHERE o.dealer_id = ? ORDER BY o.created_at DESC";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dealerId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    public void updateStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    private List<Order> mapRows(ResultSet rs) throws SQLException {
        List<Order> list = new ArrayList<>();
        while (rs.next()) {
            Order o = new Order();
            o.setId(rs.getInt("id"));
            o.setQuotationId(rs.getInt("quotation_id"));
            o.setRequestId(rs.getInt("request_id"));
            o.setShopId(rs.getInt("shop_id"));
            o.setShopName(rs.getString("shop_name"));
            o.setDealerId(rs.getInt("dealer_id"));
            o.setDealerName(rs.getString("dealer_name"));
            o.setProductName(rs.getString("product_name"));
            o.setTotalAmount(rs.getDouble("total_amount"));
            o.setStatus(rs.getString("status"));
            o.setCreatedAt(rs.getString("created_at"));
            o.setDeliveryStatus(rs.getString("delivery_status"));
            o.setDeliveryLocation(rs.getString("delivery_location"));
            o.setWeatherNote(rs.getString("weather_note"));
            o.setEstimatedArrival(rs.getString("estimated_arrival"));
            list.add(o);
        }
        return list;
    }
}