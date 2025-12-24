package org.lomeu.infrastructure.database;

import org.lomeu.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String URL =
        System.getenv().get("DB_URL");

    private static final String USER =
        System.getenv().get("DB_USER");

    private static final String PASS =
        System.getenv().get("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            AppConfig.dbUrl(),
            AppConfig.dbUser(),
            AppConfig.dbPassword()
        );
    }
}
