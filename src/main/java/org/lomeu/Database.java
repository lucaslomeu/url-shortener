package org.lomeu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class Database {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/urlshortener";
    private static final String DB_USER = "lucas";
    private static final String DB_PASSWORD = "244000";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void save(Url url) {
        String insertSql = "INSERT INTO urls (short_code, original_url, clicks, creation_date, updated_at) VALUES (?, ?, 0, ?, ?)";
        Timestamp now = Timestamp.from(Instant.now());
        try (Connection connection = getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(insertSql);
            stmt.setString(1, url.shortCode());
            stmt.setString(2, url.originalUrl());
            stmt.setTimestamp(3, now);
            stmt.setTimestamp(4, now);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Url findByShortCode(String shortCode) {
        String selectSql = "SELECT id, short_code, original_url, clicks, creation_date, updated_at FROM urls WHERE short_code = ?";
        try (Connection connection = getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(selectSql);
            stmt.setString(1, shortCode);
            ResultSet response = stmt.executeQuery();
            if (response.next()) {
                return new Url(
                    response.getInt("id"),
                    response.getString("short_code"),
                    response.getString("original_url"),
                    response.getInt("clicks"),
                    response.getString("creation_date"),
                    response.getString("updated_at")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void incrementClicks(String shortCode) {
        String updateSql = "UPDATE urls SET clicks = clicks + 1, updated_at = ? WHERE short_code = ?";
        Timestamp now = Timestamp.from(Instant.now());
        try (Connection connection = getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(updateSql);
            stmt.setTimestamp(1, now);
            stmt.setString(2, shortCode);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
