package org.lomeu.infrastructure.database;

import org.lomeu.application.port.out.UrlRepository;
import org.lomeu.domain.Url;

import java.sql.*;
import java.util.Optional;
import javax.sql.DataSource;

import org.lomeu.infrastructure.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcUrlRepository implements UrlRepository {

    private static final String NEXT_ID_SQL = "SELECT nextval('urls_id_seq')";
    private static final Logger logger = LoggerFactory.getLogger(JdbcUrlRepository.class);
    private final DataSource dataSource;

    public JdbcUrlRepository() {
        this.dataSource = null;
    }

    public JdbcUrlRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        return Database.getConnection();
    }

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
            throw new DatabaseException("Failed to get next id", e);
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
                    logger.error("Failed to rollback transaction", ex);
                }
            }
            throw new DatabaseException("Failed to insert URL", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.warn("Failed to close connection cleanly", e);
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
            throw new DatabaseException("Failed to find URL by short code", e);
        }
    }
}
