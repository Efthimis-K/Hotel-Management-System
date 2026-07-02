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
import hotel.model.Room;
import hotel.storage.DatabaseManager;

public class RoomRepository {

    public RoomRepository() {
    }

    public void addRoom(Room room) {
        String sql = "INSERT INTO rooms (room_number, room_type, price_per_night, is_available) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, room.getRoomNumber());
            stmt.setString(2, room.getRoomType().name());
            stmt.setDouble(3, room.getPricePerNight());
            stmt.setBoolean(4, room.isAvailable());
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 19) {
                throw DuplicateResourceException.forResource("Room", "number", room.getRoomNumber());
            }
            throw StorageException.forDatabase("rooms", e);
        }
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT room_number, room_type, price_per_night, is_available FROM rooms";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rooms.add(mapRoom(rs));
            }
        } catch (SQLException e) {
            throw StorageException.forDatabase("rooms", e);
        }
        return rooms;
    }

    public Optional<Room> getRoomByNumber(int roomNumber) {
        String sql = "SELECT room_number, room_type, price_per_night, is_available FROM rooms WHERE room_number = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRoom(rs));
                }
            }
        } catch (SQLException e) {
            throw StorageException.forDatabase("rooms", e);
        }
        return Optional.empty();
    }

    public void updateRoom(Room room) {
        String sql = "UPDATE rooms SET room_type = ?, price_per_night = ?, is_available = ? WHERE room_number = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, room.getRoomType().name());
            stmt.setDouble(2, room.getPricePerNight());
            stmt.setBoolean(3, room.isAvailable());
            stmt.setInt(4, room.getRoomNumber());
            int updatedRows = stmt.executeUpdate();
            if (updatedRows == 0) {
                throw ResourceNotFoundException.forResource("Room", "number", room.getRoomNumber());
            }
        } catch (SQLException e) {
            throw StorageException.forDatabase("rooms", e);
        }
    }

    public void deleteRoom(int roomNumber) {
        String sql = "DELETE FROM rooms WHERE room_number = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomNumber);
            int deletedRows = stmt.executeUpdate();
            if (deletedRows == 0) {
                throw ResourceNotFoundException.forResource("Room", "number", roomNumber);
            }
        } catch (SQLException e) {
            throw StorageException.forDatabase("rooms", e);
        }
    }

    public List<Room> getAvailableRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT room_number, room_type, price_per_night, is_available FROM rooms WHERE is_available = 1";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rooms.add(mapRoom(rs));
            }
        } catch (SQLException e) {
            throw StorageException.forDatabase("rooms", e);
        }
        return rooms;
    }

    private Room mapRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setRoomNumber(rs.getInt("room_number"));
        room.setRoomType(hotel.model.RoomType.valueOf(rs.getString("room_type")));
        room.setPricePerNight(rs.getDouble("price_per_night"));
        room.setAvailable(rs.getBoolean("is_available"));
        return room;
    }
}
