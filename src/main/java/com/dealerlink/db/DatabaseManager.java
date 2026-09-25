package com.dealerlink.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles SQLite connection creation and schema initialization.
 * The database file (dealerlink.db) is created in the project's working directory.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:dealerlink.db";

    public static Connection connect() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement s = conn.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void initialize() {
        String[] ddl = {
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT UNIQUE NOT NULL," +
                        "password TEXT NOT NULL," +
                        "role TEXT NOT NULL CHECK(role IN ('SHOP','DEALER'))," +
                        "name TEXT NOT NULL," +
                        "phone TEXT," +
                        "address TEXT," +
                        "city TEXT," +
                        "created_at TEXT NOT NULL" +
                        ");",

                "CREATE TABLE IF NOT EXISTS products (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "category TEXT," +
                        "unit TEXT" +
                        ");",

                "CREATE TABLE IF NOT EXISTS inventory (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "dealer_id INTEGER NOT NULL REFERENCES users(id)," +
                        "product_id INTEGER NOT NULL REFERENCES products(id)," +
                        "quantity_available INTEGER NOT NULL DEFAULT 0," +
                        "unit_price REAL NOT NULL DEFAULT 0," +
                        "updated_at TEXT NOT NULL," +
                        "UNIQUE(dealer_id, product_id)" +
                        ");",

                "CREATE TABLE IF NOT EXISTS requests (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "shop_id INTEGER NOT NULL REFERENCES users(id)," +
                        "product_id INTEGER NOT NULL REFERENCES products(id)," +
                        "quantity INTEGER NOT NULL," +
                        "notes TEXT," +
                        "status TEXT NOT NULL DEFAULT 'OPEN'," +
                        "created_at TEXT NOT NULL" +
                        ");",

                "CREATE TABLE IF NOT EXISTS quotations (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "request_id INTEGER NOT NULL REFERENCES requests(id)," +
                        "dealer_id INTEGER NOT NULL REFERENCES users(id)," +
                        "price_per_unit REAL NOT NULL," +
                        "total_price REAL NOT NULL," +
                        "estimated_delivery_days INTEGER," +
                        "message TEXT," +
                        "status TEXT NOT NULL DEFAULT 'PENDING'," +
                        "created_at TEXT NOT NULL" +
                        ");",

                "CREATE TABLE IF NOT EXISTS orders (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "quotation_id INTEGER NOT NULL REFERENCES quotations(id)," +
                        "request_id INTEGER NOT NULL REFERENCES requests(id)," +
                        "shop_id INTEGER NOT NULL REFERENCES users(id)," +
                        "dealer_id INTEGER NOT NULL REFERENCES users(id)," +
                        "total_amount REAL NOT NULL," +
                        "status TEXT NOT NULL DEFAULT 'CONFIRMED'," +
                        "created_at TEXT NOT NULL" +
                        ");",

                "CREATE TABLE IF NOT EXISTS deliveries (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "order_id INTEGER NOT NULL UNIQUE REFERENCES orders(id)," +
                        "current_status TEXT NOT NULL DEFAULT 'PREPARING'," +
                        "current_location TEXT," +
                        "weather_note TEXT," +
                        "estimated_arrival TEXT," +
                        "updated_at TEXT NOT NULL" +
                        ");"
        };

        try (Connection conn = connect(); Statement st = conn.createStatement()) {
            for (String sql : ddl) {
                st.execute(sql);
            }
            seedProducts(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private static void seedProducts(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) AS c FROM products")) {
            rs.next();
            if (rs.getInt("c") == 0) {
                String[] seed = {
                        "Cement (bag)|Construction|bag",
                        "Steel Rod|Construction|kg",
                        "Bricks|Construction|piece",
                        "Sand|Construction|cft",
                        "PVC Pipe|Plumbing|piece",
                        "Wall Paint|Finishing|liter",
                        "Ceramic Tiles|Finishing|sq.ft",
                        "Electrical Wire|Electrical|meter"
                };
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO products(name, category, unit) VALUES (?, ?, ?)")) {
                    for (String row : seed) {
                        String[] parts = row.split("\\|");
                        ps.setString(1, parts[0]);
                        ps.setString(2, parts[1]);
                        ps.setString(3, parts[2]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }
    }
}