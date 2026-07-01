package hotel.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.sql.Connection;
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

        Connection conn = DatabaseManager.getInstance().getConnection();
        int successCount = 0;
        try {
            conn.setAutoCommit(false);
            for (hotel.model.Customer customer : customers) {
                int rows = insertCustomer(conn, customer);
                if (rows > 0) {
                    successCount++;
                } else {
                    LOGGER.fine("Duplicate customer ignored during import: " + customer.getCustomerId());
                }
            }
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
        LOGGER.info("Migrated " + successCount + "/" + customers.size() + " customers.");
    }

    public static void importRooms(File roomsFile) throws Exception {
        List<hotel.model.Room> rooms = objectMapper.readValue(
                roomsFile, new TypeReference<List<hotel.model.Room>>() {
        });
        if (rooms.isEmpty()) {
            LOGGER.info("No rooms to migrate.");
            return;
        }

        Connection conn = DatabaseManager.getInstance().getConnection();
        int successCount = 0;
        try {
            conn.setAutoCommit(false);
            for (hotel.model.Room room : rooms) {
                int rows = insertRoom(conn, room);
                if (rows > 0) {
                    successCount++;
                } else {
                    LOGGER.fine("Duplicate room ignored during import: " + room.getRoomNumber());
                }
            }
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
        LOGGER.info("Migrated " + successCount + "/" + rooms.size() + " rooms.");
    }

    public static void importReservations(File reservationsFile) throws Exception {
        List<hotel.model.Reservation> reservations = objectMapper.readValue(
                reservationsFile, new TypeReference<List<hotel.model.Reservation>>() {
        });
        if (reservations.isEmpty()) {
            LOGGER.info("No reservations to migrate.");
            return;
        }

        Connection conn = DatabaseManager.getInstance().getConnection();
        int successCount = 0;
        try {
            conn.setAutoCommit(false);
            for (hotel.model.Reservation reservation : reservations) {
                int rows = insertReservation(conn, reservation);
                if (rows > 0) {
                    successCount++;
                } else {
                    LOGGER.fine("Duplicate reservation ignored during import: " + reservation.getReservationId());
                }
            }
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
        LOGGER.info("Migrated " + successCount + "/" + reservations.size() + " reservations.");
    }

    private static int insertCustomer(Connection conn, hotel.model.Customer customer) throws SQLException {
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

    private static int insertRoom(Connection conn, hotel.model.Room room) throws SQLException {
        String sql = "INSERT OR IGNORE INTO rooms (room_number, room_type, price_per_night, is_available) VALUES (?, ?, ?, ?)";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, room.getRoomNumber());
            stmt.setString(2, room.getRoomType().name());
            stmt.setDouble(3, room.getPricePerNight());
            stmt.setBoolean(4, room.isAvailable());
            return stmt.executeUpdate();
        }
    }

    private static int insertReservation(Connection conn, hotel.model.Reservation reservation) throws SQLException {
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