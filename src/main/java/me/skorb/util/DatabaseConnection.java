package me.skorb.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {

    private static String DB_DRIVER;
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static String DB_INIT_SCRIPT;

    private static String DB_NAME;
    private static String DB_URL_BASE;

    static {
        try {
            // Load properties from properties file
            Properties properties = new Properties();
            try (FileInputStream fis = new FileInputStream("config.properties")) {
                properties.load(fis);
            }

            DB_DRIVER = properties.getProperty("db.driver");
            DB_URL = properties.getProperty("db.url");
            DB_USER = properties.getProperty("db.user");
            DB_PASSWORD = properties.getProperty("db.password");
            DB_INIT_SCRIPT = properties.getProperty("db.init");

            // Ensuring database driver is loaded
            Class.forName(DB_DRIVER);

            if (DB_DRIVER.contains("mysql")) {
                DB_URL_BASE = properties.getProperty("db.url.base");
                DB_NAME = properties.getProperty("db.name");
                createMySQLDatabaseIfNotExists();
            }

            initDB();

            if (Boolean.parseBoolean(properties.getProperty("db.populate", "true"))) {
                populateDB();
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load database configuration file", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Database Driver not found!", e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static void createMySQLDatabaseIfNotExists() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL_BASE, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
        }
    }

    private static void initDB() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = Files.readString(Paths.get(DB_INIT_SCRIPT));

            for (String command : sql.split(";")) {
                if (!command.trim().isEmpty()) {
                    stmt.execute(command.trim());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void populateDB() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = Files.readString(Paths.get("db/populate_db.sql"));

            for (String command : sql.split(";")) {
                if (!command.trim().isEmpty()) {
                    stmt.execute(command.trim());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
