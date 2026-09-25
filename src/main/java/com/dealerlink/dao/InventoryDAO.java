package com.dealerlink.dao;

import com.dealerlink.db.DatabaseManager;
import com.dealerlink.model.InventoryItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public List<InventoryItem> getInventoryByDealer(int dealerId) throws SQLException {
        String sql = "SELECT i.*, p.name AS product_name, p.unit AS unit " +
                "FROM inventory i JOIN products p ON i.product_id = p.id " +
                "WHERE i.dealer_id = ? ORDER BY p.name";
        List<InventoryItem> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dealerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Inserts a new inventory row, or updates quantity/price if the dealer already stocks this product. */
    public void upsert(int dealerId, int productId, int quantity, double price) throws SQLException {
        String sql = "INSERT INTO inventory(dealer_id, product_id, quantity_available, unit_price, updated_at) " +
                "VALUES (?,?,?,?,?) " +
                "ON CONFLICT(dealer_id, product_id) DO UPDATE SET " +
                "quantity_available = excluded.quantity_available, " +
                "unit_price = excluded.unit_price, " +
                "updated_at = excluded.updated_at";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dealerId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            ps.setDouble(4, price);
            ps.setString(5, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    private InventoryItem mapRow(ResultSet rs) throws SQLException {
        InventoryItem item = new InventoryItem();
        item.setId(rs.getInt("id"));
        item.setDealerId(rs.getInt("dealer_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setProductName(rs.getString("product_name"));
        item.setUnit(rs.getString("unit"));
        item.setQuantityAvailable(rs.getInt("quantity_available"));
        item.setUnitPrice(rs.getDouble("unit_price"));
        item.setUpdatedAt(rs.getString("updated_at"));
        return item;
    }
}