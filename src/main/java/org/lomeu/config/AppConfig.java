package org.lomeu.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load application properties", e);
        }
    }

    private static String envOrProp(String env, String prop) {
        String value = System.getenv(env);
        if (value != null && !value.isBlank()) return value;
        return props.getProperty(prop);
    }

    public static String dbUrl() {
        return envOrProp("DB_URL", "db.url");
    }

    public static String dbUser() {
        return envOrProp("DB_USER", "db.user");
    }

    public static String dbPassword() {
        return envOrProp("DB_PASSWORD", "db.password");
    }
}