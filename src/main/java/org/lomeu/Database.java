package org.lomeu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/urlshortener";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void save(Url url) {
        String insertSql = "INSERT INTO urls (short_code, original_url) VALUES (?, ?)";

        try (Connection connection = getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(insertSql);

            stmt.setString(1, url.shortCode());
            stmt.setString(2, url.originalUrl());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Url findByShortCode(String shortCode) {
        String selectSql = "SELECT id, short_code, original_url FROM urls WHERE short_code = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(selectSql);
            stmt.setString(1, shortCode);
            var rs = stmt.executeQuery();

            if (rs.next()) {
                return new Url(rs.getInt("id"), rs.getString("short_code"), rs.getString("original_url"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
