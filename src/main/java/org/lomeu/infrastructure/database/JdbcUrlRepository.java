package org.lomeu.infrastructure.database;

import org.lomeu.application.port.out.UrlRepository;
import org.lomeu.domain.Url;

import java.sql.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.lomeu.infrastructure.database.Database.getConnection;

public class JdbcUrlRepository implements UrlRepository {

    @Override
    public Url save(Url url) {
        String insertSql = """
                INSERT INTO urls (short_code, long_url, created_at)
                VALUES (?, ?, ?)
                RETURNING *
            """;

        try (Connection connection = getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(insertSql);
            Timestamp now = Timestamp.from(Instant.from(ZonedDateTime.now()));

            stmt.setString(1, url.shortCode());
            stmt.setString(2, url.longUrl());
            stmt.setTimestamp(3, now);

            ResultSet rs = stmt.executeQuery();
            rs.next();

            return new Url(
                rs.getInt("id"),
                rs.getString("short_code"),
                rs.getString("long_url"),
                rs.getTimestamp("created_at").toInstant().atZone(ZonedDateTime.now().getZone())
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Url> findByShortCode(String shortCode) {
        String sql = "SELECT * FROM urls WHERE short_code = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, shortCode);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return Optional.empty();

            return Optional.of(new Url(
                rs.getInt("id"),
                rs.getString("short_code"),
                rs.getString("long_url"),
                rs.getTimestamp("created_at").toInstant().atZone(ZonedDateTime.now().getZone())
            ));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
