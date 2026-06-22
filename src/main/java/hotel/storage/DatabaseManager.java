package hotel.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * DatabaseManager provides a singleton database connection for SQLite.
 * Uses connection pooling (basic) and ensures proper resource management.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:data/hotel.db";
    private static DatabaseManager instance;
    private Connection connection;

    /**
     * Prevents instantiation from outside the class to enforce the singleton pattern.
     */
    ```
    private DatabaseManager() {
        // Private constructor for singleton
    }

    /**
     * Retrieves the singleton DatabaseManager instance.
     *
     * @return the singleton instance of DatabaseManager
     */
    ```
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Retrieves or lazily initializes the singleton database connection.
     *
     * On the first invocation, creates a new SQLite connection and enables
     * foreign key constraint enforcement. Subsequent calls return the cached
     * connection.
     *
     * @return the SQLite database connection
     * @throws RuntimeException if the connection cannot be established
     */
    public synchronized Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DB_URL);
                // Enable foreign key constraints
                try (var stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to establish database connection", e);
            }
        }
        return connection;
    }

    /**
     * Closes the database connection and releases its resources.
     */
    public synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Warning: Failed to close database connection: " + e.getMessage());
            }
            connection = null;
        }
    }

    /**
     * Closes the database connection of the singleton instance.
     */
    public static synchronized void shutdown() {
        if (instance != null && instance.connection != null) {
            instance.closeConnection();
        }
    }
}