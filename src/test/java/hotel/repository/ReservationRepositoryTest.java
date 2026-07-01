package hotel.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import hotel.model.Reservation;
import hotel.model.ReservationStatus;
import hotel.storage.DatabaseManager;

class ReservationRepositoryTest {

    private static final String TEST_DB_URL = "jdbc:sqlite::memory:";

    private static Connection connection;
    private static DatabaseManager databaseManager;
    private static Field databaseConnectionField;
    private static ReservationRepository reservationRepository;

    @BeforeAll
    static void initSchemaAndRepository() throws Exception {
        connection = DriverManager.getConnection(TEST_DB_URL);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("""
                CREATE TABLE customers (
                    customer_id TEXT PRIMARY KEY,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    email TEXT NOT NULL,
                    phone_number TEXT NOT NULL
                )
            """);
            stmt.execute("""
                CREATE TABLE rooms (
                    room_number INTEGER PRIMARY KEY,
                    room_type TEXT NOT NULL,
                    price_per_night REAL NOT NULL,
                    is_available INTEGER NOT NULL,
                    CHECK (room_number > 0),
                    CHECK (price_per_night > 0)
                )
            """);
            stmt.execute("""
                CREATE TABLE reservations (
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

        databaseManager = DatabaseManager.getInstance();
        databaseConnectionField = DatabaseManager.class.getDeclaredField("connection");
        databaseConnectionField.setAccessible(true);
        databaseConnectionField.set(databaseManager, connection);
        reservationRepository = new ReservationRepository();
    }

    @AfterEach
    void clearTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM reservations");
            stmt.executeUpdate("DELETE FROM rooms");
            stmt.executeUpdate("DELETE FROM customers");
        }
    }

    @AfterAll
    static void cleanup() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        if (databaseConnectionField != null && databaseManager != null) {
            databaseConnectionField.set(databaseManager, null);
        }
    }

    private void seedReservation(String reservationId, String customerId, int roomNumber,
                                 LocalDate checkIn, LocalDate checkOut, String status) {
        seedCustomer(customerId);
        seedRoom(roomNumber);

        String sql = """
            INSERT INTO reservations (
                reservation_id, customer_id, room_number, check_in_date, check_out_date, status
            ) VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, reservationId);
            stmt.setString(2, customerId);
            stmt.setInt(3, roomNumber);
            stmt.setString(4, checkIn.toString());
            stmt.setString(5, checkOut.toString());
            stmt.setString(6, status);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void seedCustomer(String customerId) {
        String sql = """
            INSERT INTO customers (customer_id, first_name, last_name, email, phone_number)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            stmt.setString(2, "Test");
            stmt.setString(3, "Customer");
            stmt.setString(4, customerId + "@example.com");
            stmt.setString(5, "+10000000000");
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void seedRoom(int roomNumber) {
        String sql = """
            INSERT INTO rooms (room_number, room_type, price_per_night, is_available)
            VALUES (?, ?, ?, ?)
        """;
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomNumber);
            stmt.setString(2, "SINGLE");
            stmt.setDouble(3, 100.0);
            stmt.setInt(4, 1);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getReservationsByDateRangeReturnsReservationsWithCheckInBeforeEndAndCheckOutAfterStart() {
        LocalDate start = LocalDate.of(2026, 7, 10);
        LocalDate end = LocalDate.of(2026, 7, 15);

        seedReservation("RES-1", "CUST-1", 101, LocalDate.of(2026, 7, 5), LocalDate.of(2026, 7, 8), ReservationStatus.COMPLETED.name());
        seedReservation("RES-2", "CUST-2", 102, LocalDate.of(2026, 7, 12), LocalDate.of(2026, 7, 14), ReservationStatus.CONFIRMED.name());
        seedReservation("RES-3", "CUST-3", 103, LocalDate.of(2026, 7, 8), LocalDate.of(2026, 7, 10), ReservationStatus.CONFIRMED.name());
        seedReservation("RES-4", "CUST-4", 104, LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 17), ReservationStatus.CONFIRMED.name());
        seedReservation("RES-5", "CUST-5", 105, LocalDate.of(2026, 7, 18), LocalDate.of(2026, 7, 20), ReservationStatus.CONFIRMED.name());

        List<Reservation> result = reservationRepository.getReservationsByDateRange(start, end);

        assertEquals(3, result.size());
        assertEquals(List.of("RES-2", "RES-3", "RES-4"),
                result.stream().map(Reservation::getReservationId).sorted().toList());
    }

    @Test
    void getReservationsByDateRangeIncludesExactRangeMatch() {
        LocalDate start = LocalDate.of(2026, 7, 20);
        LocalDate end = LocalDate.of(2026, 7, 22);

        seedReservation("RES-EXACT", "CUST-1", 201, start, end, ReservationStatus.CONFIRMED.name());

        List<Reservation> result = reservationRepository.getReservationsByDateRange(start, end);

        assertEquals(1, result.size());
        assertEquals("RES-EXACT", result.get(0).getReservationId());
    }

    @Test
    void getReservationsByDateRangeIncludesCheckOutEqualToStart() {
        LocalDate start = LocalDate.of(2026, 7, 25);
        LocalDate end = LocalDate.of(2026, 7, 27);

        seedReservation("RES-CHECKOUT-START", "CUST-1", 202, LocalDate.of(2026, 7, 20), start, ReservationStatus.CONFIRMED.name());

        List<Reservation> result = reservationRepository.getReservationsByDateRange(start, end);

        assertEquals(1, result.size());
        assertEquals("RES-CHECKOUT-START", result.get(0).getReservationId());
    }

    @Test
    void getReservationsByDateRangeExcludesReservationsTotallyBeforeRange() {
        LocalDate start = LocalDate.of(2026, 7, 30);
        LocalDate end = LocalDate.of(2026, 7, 31);

        seedReservation("RES-BEFORE", "CUST-1", 203, LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 29), ReservationStatus.CONFIRMED.name());

        List<Reservation> result = reservationRepository.getReservationsByDateRange(start, end);

        assertEquals(0, result.size());
    }

    @Test
    void getReservationsByDateRangeExcludesReservationsTotallyAfterRange() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 3);

        seedReservation("RES-AFTER", "CUST-1", 204, LocalDate.of(2026, 8, 5), LocalDate.of(2026, 8, 7), ReservationStatus.CONFIRMED.name());

        List<Reservation> result = reservationRepository.getReservationsByDateRange(start, end);

        assertEquals(0, result.size());
    }

    @Test
    void getReservationsByDateRangeIncludesReservationEnclosingQueryRange() {
        LocalDate start = LocalDate.of(2026, 8, 10);
        LocalDate end = LocalDate.of(2026, 8, 12);

        seedReservation("RES-ENCLOSE", "CUST-1", 205, LocalDate.of(2026, 8, 8), LocalDate.of(2026, 8, 14), ReservationStatus.CONFIRMED.name());

        List<Reservation> result = reservationRepository.getReservationsByDateRange(start, end);

        assertEquals(1, result.size());
        assertEquals("RES-ENCLOSE", result.get(0).getReservationId());
    }

    @Test
    void getReservationsByDateRangeReturnsEmptyListForNoMatches() {
        List<Reservation> result = reservationRepository.getReservationsByDateRange(
                LocalDate.of(2099, 1, 1), LocalDate.of(2099, 1, 31));
        assertEquals(0, result.size());
    }
}
