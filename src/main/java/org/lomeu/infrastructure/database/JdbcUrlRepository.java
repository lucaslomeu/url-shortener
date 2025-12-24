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
                INSERT INTO urls (short_code, original_url, clicks, creation_date, updated_at)
                VALUES (?, ?, 0, ?, ?)
                RETURNING *
            """;

        try (Connection connection = getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(insertSql);
            Timestamp now = Timestamp.from(Instant.from(ZonedDateTime.now()));

            stmt.setString(1, url.shortCode());
            stmt.setString(2, url.originalUrl());
            stmt.setTimestamp(3, now);
            stmt.setTimestamp(4, now);

            ResultSet rs = stmt.executeQuery();
            rs.next();

            return new Url(
                rs.getInt("id"),
                rs.getString("short_code"),
                rs.getString("original_url"),
                rs.getInt("clicks"),
                rs.getTimestamp("creation_date").toInstant().atZone(ZonedDateTime.now().getZone()),
                rs.getTimestamp("updated_at").toInstant().atZone(ZonedDateTime.now().getZone())
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
                rs.getString("original_url"),
                rs.getInt("clicks"),
                rs.getTimestamp("creation_date").toInstant().atZone(ZonedDateTime.now().getZone()),
                rs.getTimestamp("updated_at").toInstant().atZone(ZonedDateTime.now().getZone())
            ));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void incrementClicks(String shortCode) {
        String sql = """
            UPDATE urls
            SET clicks = clicks + 1, updated_at = ?
            WHERE short_code = ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.from(Instant.now()));
            ps.setString(2, shortCode);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
