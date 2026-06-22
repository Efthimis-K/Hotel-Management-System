package hotel.storage;

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Handles the one-time migration of JSON data to SQLite. Reads data from
 * data/customers.json, data/rooms.json, and data/reservations.json and inserts
 * them into the SQLite database tables.
 */
public class DatabaseInitializer {

    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());
    private static final String DATA_DIR = "data";

    public static void initialize() {
        File dbFile = new File(DATA_DIR, "hotel.db");
        File customersJson = new File(DATA_DIR, "customers.json");
        File roomsJson = new File(DATA_DIR, "rooms.json");
        File reservationsJson = new File(DATA_DIR, "reservations.json");

        boolean dbExisted = dbFile.exists();

        LOGGER.info("Initializing database schema...");
        try {
            initSchema();
        } catch (Exception e) {
            LOGGER.severe("Schema initialization failed: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }

        // If the DB already existed AND already has data, skip migration.
        // If the DB is new or empty, import from JSON files.
        if (dbExisted && isDataAlreadyImported()) {
            LOGGER.info("Database already exists with data, skipping JSON migration.");
            return;
        }

        LOGGER.info("Migrating JSON data to database...");

        try {
            // Migrate customers first (reservations depend on them via FK)
            if (customersJson.exists()) {
                LOGGER.info("Migrating customers from " + customersJson.getPath());
                JsonImportUtil.importCustomers(customersJson);
            }

            // Migrate rooms next (reservations depend on them via FK)
            if (roomsJson.exists()) {
                LOGGER.info("Migrating rooms from " + roomsJson.getPath());
                JsonImportUtil.importRooms(roomsJson);
            }

            // Migrate reservations last
            if (reservationsJson.exists()) {
                LOGGER.info("Migrating reservations from " + reservationsJson.getPath());
                JsonImportUtil.importReservations(reservationsJson);
            }

            LOGGER.info("Database initialization completed successfully.");
        } catch (Exception e) {
            LOGGER.severe("Database initialization failed: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Checks whether the database already contains data. Used to avoid
     * re-importing JSON data on every startup.
     */
    private static boolean isDataAlreadyImported() {
        var conn = DatabaseManager.getInstance().getConnection();
        try (var stmt = conn.createStatement(); var rs = stmt.executeQuery(
                "SELECT (SELECT COUNT(*) FROM customers) + (SELECT COUNT(*) FROM rooms) + (SELECT COUNT(*) FROM reservations) AS total")) {
            rs.next();
            return rs.getLong("total") > 0;
        } catch (SQLException e) {
            LOGGER.warning("Could not check if data is already imported: " + e.getMessage());
            return false;
        }
    }

    private static void initSchema() throws SQLException {
        var conn = DatabaseManager.getInstance().getConnection();
        // Enable foreign key constraints
        try (var stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }

        // Create schema_version table
        try (var stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS schema_version (
                    version INTEGER PRIMARY KEY
                )
            """);
        }

        // Check if schema_version table is empty and initialize if needed
        long count = 0;
        try (var stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM schema_version");
            rs.next();
            count = rs.getLong(1);
        }

        if (count == 0) {
            try (var stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO schema_version (version) VALUES (1)");
            }
        }

        // Create customers table
        try (var stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    customer_id TEXT PRIMARY KEY,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    email TEXT NOT NULL,
                    phone_number TEXT NOT NULL
                )
            """);
        }

        // Create rooms table
        try (var stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS rooms (
                    room_number INTEGER PRIMARY KEY,
                    room_type TEXT NOT NULL,
                    price_per_night REAL NOT NULL,
                    is_available INTEGER NOT NULL,
                    CHECK (room_number > 0),
                    CHECK (price_per_night > 0)
                )
            """);
        }

        // Create reservations table
        try (var stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reservations (
                    reservation_id TEXT PRIMARY KEY,
                    customer_id TEXT NOT NULL,
                    room_number INTEGER NOT NULL,
                    check_in_date TEXT NOT NULL,
                    check_out_date TEXT NOT NULL,
                    status TEXT NOT NULL,
                    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
                    FOREIGN KEY (room_number) REFERENCES rooms(room_number)
                )
            """);
        }

        // CREATE INDEX for better performance
        try (var stmt = conn.createStatement()) {
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_reservations_customer_id ON reservations (customer_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_reservations_room_number ON reservations (room_number)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_reservations_dates ON reservations (check_in_date, check_out_date)");
        }

        LOGGER.fine("Database schema initialized successfully.");
    }
}
