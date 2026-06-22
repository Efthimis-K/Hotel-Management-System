package hotel.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utility class for importing data from JSON files to the SQLite database.
 * Handles the one-time data migration from JSON files to SQL tables.
 */
public class JsonImportUtil {

    private static final Logger LOGGER = Logger.getLogger(JsonImportUtil.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Imports customer data from a JSON file into the database.
     *
     * @param customersFile the JSON file containing customer data
     * @throws Exception if JSON parsing fails or if all customer imports fail
     */
    public static void importCustomers(File customersFile) throws Exception {
        List<hotel.model.Customer> customers = objectMapper.readValue(
                customersFile, new TypeReference<List<hotel.model.Customer>>() {
        });
        if (customers.isEmpty()) {
            LOGGER.info("No customers to migrate.");
            return;
        }

        var conn = DatabaseManager.getInstance().getConnection();
        int successCount = 0;
        int failCount = 0;
        for (hotel.model.Customer customer : customers) {
            try {
                insertCustomer(conn, customer);
                successCount++;
            } catch (Exception e) {
                failCount++;
                LOGGER.warning("Failed to insert customer " + customer.getCustomerId() + ": " + e.getMessage());
            }
        }
        LOGGER.info("Migrated " + successCount + "/" + customers.size() + " customers."
                + (failCount > 0 ? " (" + failCount + " failed)" : ""));
        if (failCount > 0 && successCount == 0) {
            throw new RuntimeException("All " + failCount + " customer imports failed");
        }
    }

    /**
     * Migrates rooms from a JSON file into the database.
     *
     * @param roomsFile the JSON file containing room data
     * @throws Exception if the JSON file cannot be parsed
     * @throws RuntimeException if all room imports fail
     */
    public static void importRooms(File roomsFile) throws Exception {
        List<hotel.model.Room> rooms = objectMapper.readValue(
                roomsFile, new TypeReference<List<hotel.model.Room>>() {
        });
        if (rooms.isEmpty()) {
            LOGGER.info("No rooms to migrate.");
            return;
        }

        var conn = DatabaseManager.getInstance().getConnection();
        int successCount = 0;
        int failCount = 0;
        for (hotel.model.Room room : rooms) {
            try {
                insertRoom(conn, room);
                successCount++;
            } catch (Exception e) {
                failCount++;
                LOGGER.warning("Failed to insert room " + room.getRoomNumber() + ": " + e.getMessage());
            }
        }
        LOGGER.info("Migrated " + successCount + "/" + rooms.size() + " rooms."
                + (failCount > 0 ? " (" + failCount + " failed)" : ""));
        if (failCount > 0 && successCount == 0) {
            throw new RuntimeException("All " + failCount + " room imports failed");
        }
    }

    /**
     * Migrates reservation data from a JSON file into the database.
     *
     * <p>Deserializes the provided JSON file into a list of reservations and attempts to insert
     * each one. Individual insertion failures are logged and do not halt the import process.
     *
     * @param reservationsFile a JSON file containing an array of reservation objects
     * @throws Exception if JSON deserialization fails
     * @throws RuntimeException if all reservation imports fail
     */
    public static void importReservations(File reservationsFile) throws Exception {
        List<hotel.model.Reservation> reservations = objectMapper.readValue(
                reservationsFile, new TypeReference<List<hotel.model.Reservation>>() {
        });
        if (reservations.isEmpty()) {
            LOGGER.info("No reservations to migrate.");
            return;
        }

        var conn = DatabaseManager.getInstance().getConnection();
        int successCount = 0;
        int failCount = 0;
        for (hotel.model.Reservation reservation : reservations) {
            try {
                insertReservation(conn, reservation);
                successCount++;
            } catch (Exception e) {
                failCount++;
                LOGGER.warning("Failed to insert reservation " + reservation.getReservationId() + ": " + e.getMessage());
            }
        }
        LOGGER.info("Migrated " + successCount + "/" + reservations.size() + " reservations."
                + (failCount > 0 ? " (" + failCount + " failed)" : ""));
        if (failCount > 0 && successCount == 0) {
            throw new RuntimeException("All " + failCount + " reservation imports failed");
        }
    }

    /**
     * Inserts a customer record into the database, silently ignoring duplicates.
     *
     * @throws SQLException if a database error occurs
     */
    private static void insertCustomer(java.sql.Connection conn, hotel.model.Customer customer) throws SQLException {
        String sql = "INSERT OR IGNORE INTO customers (customer_id, first_name, last_name, email, phone_number) VALUES (?, ?, ?, ?, ?)";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getCustomerId());
            stmt.setString(2, customer.getFirstName());
            stmt.setString(3, customer.getLastName());
            stmt.setString(4, customer.getEmail());
            stmt.setString(5, customer.getPhoneNumber());
            stmt.executeUpdate();
        }
    }

    /**
     * Inserts a room record into the database, ignoring duplicates.
     *
     * @throws SQLException if a database error occurs
     */
    private static void insertRoom(java.sql.Connection conn, hotel.model.Room room) throws SQLException {
        String sql = "INSERT OR IGNORE INTO rooms (room_number, room_type, price_per_night, is_available) VALUES (?, ?, ?, ?)";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, room.getRoomNumber());
            stmt.setString(2, room.getRoomType().name());
            stmt.setDouble(3, room.getPricePerNight());
            stmt.setBoolean(4, room.isAvailable());
            stmt.executeUpdate();
        }
    }

    /**
     * Inserts a reservation record into the database, ignoring any duplicate key conflicts.
     *
     * @param conn the database connection
     * @param reservation the reservation to insert
     * @throws SQLException if a database access error occurs
     */
    private static void insertReservation(java.sql.Connection conn, hotel.model.Reservation reservation) throws SQLException {
        String sql = "INSERT OR IGNORE INTO reservations (reservation_id, customer_id, room_number, check_in_date, check_out_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reservation.getReservationId());
            stmt.setString(2, reservation.getCustomerId());
            stmt.setInt(3, reservation.getRoomNumber());
            stmt.setString(4, reservation.getCheckInDate().toString());
            stmt.setString(5, reservation.getCheckOutDate().toString());
            stmt.setString(6, reservation.getStatus().name());
            stmt.executeUpdate();
        }
    }
}
