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
            if (e.getErrorCode() == 19) {
                throw DuplicateResourceException.forResource("Reservation", "ID", reservation.getReservationId());
            }
            throw StorageException.forDatabase("reservations", e);
        }
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT reservation_id, customer_id, room_number, check_in_date, check_out_date, status FROM reservations";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reservations.add(mapReservation(rs));
            }
        } catch (SQLException e) {
            throw StorageException.forDatabase("reservations", e);
        }
        return reservations;
    }

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
            throw StorageException.forDatabase("reservations", e);
        }
        return Optional.empty();
    }

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
            throw StorageException.forDatabase("reservations", e);
        }
    }

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
            throw StorageException.forDatabase("reservations", e);
        }
    }

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
            throw StorageException.forDatabase("reservations", e);
        }
        return reservations;
    }

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
            throw StorageException.forDatabase("reservations", e);
        }
        return reservations;
    }

    public List<Reservation> getActiveReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT reservation_id, customer_id, room_number, check_in_date, check_out_date, status FROM reservations WHERE status IN ('CONFIRMED', 'PENDING')";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reservations.add(mapReservation(rs));
            }
        } catch (SQLException e) {
            throw StorageException.forDatabase("reservations", e);
        }
        return reservations;
    }

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setReservationId(rs.getString("reservation_id"));
        reservation.setCustomerId(rs.getString("customer_id"));
        reservation.setRoomNumber(rs.getInt("room_number"));
        try {
            reservation.setCheckInDate(java.time.LocalDate.parse(rs.getString("check_in_date")));
            reservation.setCheckOutDate(java.time.LocalDate.parse(rs.getString("check_out_date")));
        } catch (Exception e) {
            throw new SQLException("Invalid date format in reservation record: " + e.getMessage(), e);
        }
        reservation.setStatus(ReservationStatus.valueOf(rs.getString("status")));
        return reservation;
    }
}
