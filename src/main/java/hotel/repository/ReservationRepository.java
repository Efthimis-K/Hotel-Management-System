package hotel.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import hotel.exception.DuplicateResourceException;
import hotel.exception.ResourceNotFoundException;
import hotel.exception.StorageException;
import hotel.model.Reservation;
import hotel.model.ReservationStatus;
import hotel.storage.DatabaseManager;

public class ReservationRepository {

    /**
     * Stores a reservation in the database.
     *
     * @param reservation the reservation to add
     * @throws DuplicateResourceException if a reservation with the same ID already exists
     * @throws StorageException if a database error occurs
     */
    public void addReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (reservation_id, customer_id, room_number, check_in_date, check_out_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reservation.getReservationId());
            stmt.setString(2, reservation.getCustomerId());
            stmt.setInt(3, reservation.getRoomNumber());
            stmt.setString(4, reservation.getCheckInDate().toString());
            stmt.setString(5, reservation.getCheckOutDate().toString());
            stmt.setString(6, reservation.getStatus().name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().contains("SQLITE_CONSTRAINT_UNIQUE")) {
                throw DuplicateResourceException.forResource("Reservation", "ID", reservation.getReservationId());
            }
            throw StorageException.forFile("data/reservations.json", e);
        }
    }

    /**
     * Retrieves all reservations from the database.
     *
     * @return a list of all reservations
     * @throws StorageException if a database access error occurs
     */
    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT reservation_id, customer_id, room_number, check_in_date, check_out_date, status FROM reservations";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reservations.add(mapReservation(rs));
            }
        } catch (SQLException e) {
            throw StorageException.forFile("data/reservations.json", e);
        }
        return reservations;
    }

    /**
     * Retrieves the reservation with the specified ID.
     *
     * @param reservationId the ID of the reservation to retrieve
     * @return an Optional containing the reservation if found, or empty if the reservation does not exist or if the ID is null
     * @throws StorageException if a database error occurs
     */
    public Optional<Reservation> getReservationById(String reservationId) {
        if (reservationId == null) {
            return Optional.empty();
        }
        String sql = "SELECT reservation_id, customer_id, room_number, check_in_date, check_out_date, status FROM reservations WHERE reservation_id = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reservationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw StorageException.forFile("data/reservations.json", e);
        }
        return Optional.empty();
    }

    /**
     * Updates an existing reservation with the provided information.
     *
     * @param reservation the reservation object containing the updated information
     * @throws ResourceNotFoundException if no reservation with the given ID exists
     * @throws StorageException if a database error occurs
     */
    public void updateReservation(Reservation reservation) {
        String sql = "UPDATE reservations SET customer_id = ?, room_number = ?, check_in_date = ?, check_out_date = ?, status = ? WHERE reservation_id = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reservation.getCustomerId());
            stmt.setInt(2, reservation.getRoomNumber());
            stmt.setString(3, reservation.getCheckInDate().toString());
            stmt.setString(4, reservation.getCheckOutDate().toString());
            stmt.setString(5, reservation.getStatus().name());
            stmt.setString(6, reservation.getReservationId());
            int updatedRows = stmt.executeUpdate();
            if (updatedRows == 0) {
                throw ResourceNotFoundException.forResource("Reservation", "ID", reservation.getReservationId());
            }
        } catch (SQLException e) {
            throw StorageException.forFile("data/reservations.json", e);
        }
    }

    /**
     * Deletes a reservation from storage by ID.
     *
     * @param reservationId the ID of the reservation to delete
     * @throws ResourceNotFoundException if no reservation with the given ID exists
     * @throws StorageException if a database error occurs
     */
    public void deleteReservation(String reservationId) {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reservationId);
            int deletedRows = stmt.executeUpdate();
            if (deletedRows == 0) {
                throw ResourceNotFoundException.forResource("Reservation", "ID", reservationId);
            }
        } catch (SQLException e) {
            throw StorageException.forFile("data/reservations.json", e);
        }
    }

    /**
     * Retrieves all reservations associated with a specific customer.
     *
     * @param customerId the customer's ID
     * @return a list of reservations for the customer, or an empty list if customerId is null
     * @throws StorageException if a database error occurs
     */
    public List<Reservation> getReservationsByCustomer(String customerId) {
        if (customerId == null) {
            return new ArrayList<>();
        }
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT reservation_id, customer_id, room_number, check_in_date, check_out_date, status FROM reservations WHERE customer_id = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw StorageException.forFile("data/reservations.json", e);
        }
        return reservations;
    }

    /**
     * Retrieves all reservations for a specific room.
     *
     * @param roomNumber the room number to search by
     * @return a list of reservations for the room
     * @throws StorageException if a database error occurs
     */
    public List<Reservation> getReservationsByRoom(int roomNumber) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT reservation_id, customer_id, room_number, check_in_date, check_out_date, status FROM reservations WHERE room_number = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw StorageException.forFile("data/reservations.json", e);
        }
        return reservations;
    }

    /**
     * Retrieves all active reservations.
     *
     * @return A list of reservations with status CONFIRMED or PENDING.
     */
    public List<Reservation> getActiveReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT reservation_id, customer_id, room_number, check_in_date, check_out_date, status FROM reservations WHERE status IN ('CONFIRMED', 'PENDING')";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reservations.add(mapReservation(rs));
            }
        } catch (SQLException e) {
            throw StorageException.forFile("data/reservations.json", e);
        }
        return reservations;
    }

    /**
     * Converts a database result set row into a Reservation object.
     *
     * @return a Reservation object populated from the result set row
     * @throws SQLException if a database access error occurs
     */
    private Reservation mapReservation(ResultSet rs) throws SQLException {
        // Use no-arg constructor + setters to bypass the past-date validation
        // in the 5-arg constructor — existing DB data may have past dates.
        Reservation reservation = new Reservation();
        reservation.setReservationId(rs.getString("reservation_id"));
        reservation.setCustomerId(rs.getString("customer_id"));
        reservation.setRoomNumber(rs.getInt("room_number"));
        reservation.setCheckInDate(java.time.LocalDate.parse(rs.getString("check_in_date")));
        reservation.setCheckOutDate(java.time.LocalDate.parse(rs.getString("check_out_date")));
        reservation.setStatus(ReservationStatus.valueOf(rs.getString("status")));
        return reservation;
    }
}
