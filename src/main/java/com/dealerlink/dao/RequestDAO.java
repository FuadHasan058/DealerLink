package com.dealerlink.dao;

import com.dealerlink.db.DatabaseManager;
import com.dealerlink.model.ProductRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RequestDAO {

    public int createRequest(ProductRequest req) throws SQLException {
        String sql = "INSERT INTO requests(shop_id, product_id, quantity, notes, status, created_at) " +
                "VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, req.getShopId());
            ps.setInt(2, req.getProductId());
            ps.setInt(3, req.getQuantity());
            ps.setString(4, req.getNotes());
            ps.setString(5, "OPEN");
            ps.setString(6, LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public List<ProductRequest> getRequestsByShop(int shopId) throws SQLException {
        String sql = "SELECT r.*, p.name AS product_name, p.unit AS unit, u.name AS shop_name, u.city AS shop_city " +
                "FROM requests r " +
                "JOIN products p ON r.product_id = p.id " +
                "JOIN users u ON r.shop_id = u.id " +
                "WHERE r.shop_id = ? ORDER BY r.created_at DESC";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    /** Requests that are still open for quoting (dealers browse this list). */
    public List<ProductRequest> getOpenRequests() throws SQLException {
        String sql = "SELECT r.*, p.name AS product_name, p.unit AS unit, u.name AS shop_name, u.city AS shop_city " +
                "FROM requests r " +
                "JOIN products p ON r.product_id = p.id " +
                "JOIN users u ON r.shop_id = u.id " +
                "WHERE r.status IN ('OPEN','QUOTED') ORDER BY r.created_at DESC";
        try (Connection conn = DatabaseManager.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return mapRows(rs);
        }
    }

    public void updateStatus(int requestId, String status) throws SQLException {
        String sql = "UPDATE requests SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        }
    }

    private List<ProductRequest> mapRows(ResultSet rs) throws SQLException {
        List<ProductRequest> list = new ArrayList<>();
        while (rs.next()) {
            ProductRequest r = new ProductRequest();
            r.setId(rs.getInt("id"));
            r.setShopId(rs.getInt("shop_id"));
            r.setShopName(rs.getString("shop_name"));
            r.setShopCity(rs.getString("shop_city"));
            r.setProductId(rs.getInt("product_id"));
            r.setProductName(rs.getString("product_name"));
            r.setUnit(rs.getString("unit"));
            r.setQuantity(rs.getInt("quantity"));
            r.setNotes(rs.getString("notes"));
            r.setStatus(rs.getString("status"));
            r.setCreatedAt(rs.getString("created_at"));
            list.add(r);
        }
        return list;
    }
}