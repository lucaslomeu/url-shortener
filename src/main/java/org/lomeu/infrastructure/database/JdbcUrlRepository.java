package org.lomeu.infrastructure.database;

import org.lomeu.application.port.out.UrlRepository;
import org.lomeu.domain.Url;

import java.sql.*;
import java.util.Optional;

import static org.lomeu.infrastructure.database.Database.getConnection;

public class JdbcUrlRepository implements UrlRepository {

    private static final String NEXT_ID_SQL = "SELECT nextval('urls_id_seq')";

    @Override
    public long nextId() {
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(NEXT_ID_SQL);
             ResultSet rs = stmt.executeQuery()) {

            if (!rs.next()) {
                throw new SQLException("Failed to get next id");
            }
            return rs.getLong(1);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Url insert(long id, String shortCode, String longUrl) {
        String sql = """
            INSERT INTO urls (id, short_code, long_url)
            VALUES (?, ?, ?)
            RETURNING id, short_code, long_url, created_at
            """;

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                stmt.setString(2, shortCode);
                stmt.setString(3, longUrl);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Failed to insert URL");
                    }

                    Url result = new Url(
                            rs.getLong("id"),
                            rs.getString("short_code"),
                            rs.getString("long_url"),
                            rs.getTimestamp("created_at").toInstant()
                    );

                    conn.commit();
                    return result;
                }
            }

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Optional<Url> findByShortCode(String shortCode) {
        String sql = """
            SELECT id, short_code, long_url, created_at
            FROM urls
            WHERE short_code = ?
            """;

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, shortCode);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(new Url(
                        rs.getLong("id"),
                        rs.getString("short_code"),
                        rs.getString("long_url"),
                        rs.getTimestamp("created_at").toInstant()
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}