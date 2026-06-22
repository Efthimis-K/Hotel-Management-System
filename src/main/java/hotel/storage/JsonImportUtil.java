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
                int rows = insertCustomer(conn, customer);
                if (rows > 0) {
                    successCount++;
                } else {
                    LOGGER.fine("Duplicate customer ignored during import: " + customer.getCustomerId());
                }
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
                int rows = insertRoom(conn, room);
                if (rows > 0) {
                    successCount++;
                } else {
                    LOGGER.fine("Duplicate room ignored during import: " + room.getRoomNumber());
                }
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
                int rows = insertReservation(conn, reservation);
                if (rows > 0) {
                    successCount++;
                } else {
                    LOGGER.fine("Duplicate reservation ignored during import: " + reservation.getReservationId());
                }
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

    private static int insertCustomer(java.sql.Connection conn, hotel.model.Customer customer) throws SQLException {
        String sql = "INSERT OR IGNORE INTO customers (customer_id, first_name, last_name, email, phone_number) VALUES (?, ?, ?, ?, ?)";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getCustomerId());
            stmt.setString(2, customer.getFirstName());
            stmt.setString(3, customer.getLastName());
            stmt.setString(4, customer.getEmail());
            stmt.setString(5, customer.getPhoneNumber());
            return stmt.executeUpdate();
        }
    }

    private static int insertRoom(java.sql.Connection conn, hotel.model.Room room) throws SQLException {
        String sql = "INSERT OR IGNORE INTO rooms (room_number, room_type, price_per_night, is_available) VALUES (?, ?, ?, ?)";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, room.getRoomNumber());
            stmt.setString(2, room.getRoomType().name());
            stmt.setDouble(3, room.getPricePerNight());
            stmt.setBoolean(4, room.isAvailable());
            return stmt.executeUpdate();
        }
    }

    private static int insertReservation(java.sql.Connection conn, hotel.model.Reservation reservation) throws SQLException {
        String sql = "INSERT OR IGNORE INTO reservations (reservation_id, customer_id, room_number, check_in_date, check_out_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reservation.getReservationId());
            stmt.setString(2, reservation.getCustomerId());
            stmt.setInt(3, reservation.getRoomNumber());
            stmt.setString(4, reservation.getCheckInDate().toString());
            stmt.setString(5, reservation.getCheckOutDate().toString());
            stmt.setString(6, reservation.getStatus().name());
            return stmt.executeUpdate();
        }
    }
}
