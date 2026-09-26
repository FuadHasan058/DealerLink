package com.dealerlink.dao;

import com.dealerlink.db.DatabaseManager;
import com.dealerlink.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAO {

    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY name";
        try (Connection conn = DatabaseManager.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("unit")
                ));
            }
        }
        return products;
    }

    public Optional<Product> findByName(String name) throws SQLException {
        String sql = "SELECT * FROM products WHERE LOWER(TRIM(name)) = LOWER(TRIM(?)) LIMIT 1";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getString("unit")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Looks up a product by name; if it doesn't exist, inserts it with a default category/unit and returns the ID.
     */
    public int findOrCreateProduct(String name, String defaultUnit) throws SQLException {
        Optional<Product> existing = findByName(name);
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        String unit = (defaultUnit != null && !defaultUnit.isBlank()) ? defaultUnit.trim() : "units";
        String sql = "INSERT INTO products(name, category, unit) VALUES (?, 'General', ?)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, unit);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }
}