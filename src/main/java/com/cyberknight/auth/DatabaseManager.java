package com.cyberknight.auth;

import java.sql.*;

/**
 * DatabaseManager – koneksi MySQL dan operasi tabel users.
 *
 * SETUP SEBELUM RUN:
 *   1. Pastikan MySQL Server berjalan
 *   2. Buat database: CREATE DATABASE cyberknight;
 *   3. Sesuaikan DB_USER dan DB_PASS di bawah
 *
 * Tabel users dibuat otomatis.
 */
public class DatabaseManager {

    // ── Konfigurasi — sesuaikan dengan MySQL kamu ─────────────
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "cyberknight";
    private static final String DB_USER = "root";   // ganti jika perlu
    private static final String DB_PASS = "";        // isi password MySQL kamu

    private static final String DB_URL =
        "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        + "&createDatabaseIfNotExist=true";

    private static DatabaseManager instance;
    private Connection conn;

    private DatabaseManager() {
        connect();
        if (conn != null) createTable();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("[DB] MySQL connected: " + DB_NAME);
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver tidak ada: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DB] Koneksi gagal: " + e.getMessage());
            System.err.println("[DB] Pastikan MySQL berjalan dan password benar.");
        }
    }

    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id       INT          AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(30)  UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                created  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
            System.out.println("[DB] Tabel 'users' siap.");
        } catch (SQLException e) {
            System.err.println("[DB] createTable error: " + e.getMessage());
        }
    }

    // ── Register ──────────────────────────────────────────────
    /** Return true jika berhasil, false jika username sudah ada */
    public boolean registerUser(String username, String hashedPassword) {
        if (conn == null) return false;
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false; // Duplicate entry
        }
    }

    // ── Get user ──────────────────────────────────────────────
    public UserData getUserByUsername(String username) {
        if (conn == null) return null;
        String sql = "SELECT id, username, password FROM users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new UserData(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password")
                );
            }
        } catch (SQLException e) {
            System.err.println("[DB] getUser error: " + e.getMessage());
        }
        return null;
    }

    public boolean isConnected() {
        try { return conn != null && !conn.isClosed(); }
        catch (SQLException e) { return false; }
    }

    public void close() {
        try { if (conn != null) conn.close(); }
        catch (SQLException e) { System.err.println("[DB] close error: " + e.getMessage()); }
    }
}