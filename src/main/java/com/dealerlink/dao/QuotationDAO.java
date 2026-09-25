package com.dealerlink.dao;

import com.dealerlink.db.DatabaseManager;
import com.dealerlink.model.Quotation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuotationDAO {

    public int createQuotation(Quotation q) throws SQLException {
        String sql = "INSERT INTO quotations(request_id, dealer_id, price_per_unit, total_price, " +
                "estimated_delivery_days, message, status, created_at) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, q.getRequestId());
            ps.setInt(2, q.getDealerId());
            ps.setDouble(3, q.getPricePerUnit());
            ps.setDouble(4, q.getTotalPrice());
            ps.setInt(5, q.getEstimatedDeliveryDays());
            ps.setString(6, q.getMessage());
            ps.setString(7, "PENDING");
            ps.setString(8, LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public List<Quotation> getQuotationsForRequest(int requestId) throws SQLException {
        String sql = "SELECT q.*, u.name AS dealer_name, u.city AS dealer_city " +
                "FROM quotations q JOIN users u ON q.dealer_id = u.id " +
                "WHERE q.request_id = ? ORDER BY q.price_per_unit ASC";
        List<Quotation> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void updateStatus(int quotationId, String status) throws SQLException {
        String sql = "UPDATE quotations SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, quotationId);
            ps.executeUpdate();
        }
    }

    /** Rejects every other pending quotation for a request once one has been accepted. */
    public void rejectOtherQuotations(int requestId, int acceptedQuotationId) throws SQLException {
        String sql = "UPDATE quotations SET status = 'REJECTED' WHERE request_id = ? AND id != ? AND status = 'PENDING'";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setInt(2, acceptedQuotationId);
            ps.executeUpdate();
        }
    }

    public Quotation getById(int id) throws SQLException {
        String sql = "SELECT q.*, u.name AS dealer_name, u.city AS dealer_city " +
                "FROM quotations q JOIN users u ON q.dealer_id = u.id WHERE q.id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    private Quotation mapRow(ResultSet rs) throws SQLException {
        Quotation q = new Quotation();
        q.setId(rs.getInt("id"));
        q.setRequestId(rs.getInt("request_id"));
        q.setDealerId(rs.getInt("dealer_id"));
        q.setDealerName(rs.getString("dealer_name"));
        q.setDealerCity(rs.getString("dealer_city"));
        q.setPricePerUnit(rs.getDouble("price_per_unit"));
        q.setTotalPrice(rs.getDouble("total_price"));
        q.setEstimatedDeliveryDays(rs.getInt("estimated_delivery_days"));
        q.setMessage(rs.getString("message"));
        q.setStatus(rs.getString("status"));
        q.setCreatedAt(rs.getString("created_at"));
        return q;
    }
}