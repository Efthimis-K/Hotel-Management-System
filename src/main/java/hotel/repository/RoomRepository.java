package hotel.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import hotel.exception.DuplicateResourceException;
import hotel.exception.ResourceNotFoundException;
import hotel.model.Room;
import hotel.util.JsonFileHandler;

public class RoomRepository {
    private static final String FILE_PATH = "data/rooms.json";
    private List<Room> rooms;

    public RoomRepository() {
        loadRooms();
    }

    public RoomRepository(Consumer<Room> availabilityUpdater) {
        loadRooms();
        if (availabilityUpdater != null) {
            rooms.forEach(availabilityUpdater);
        }
    }

    private void loadRooms() {
        // JsonFileHandler.loadFromFile already wraps IO and JSON errors in
        // StorageException with the file path and original cause, so we
        // simply propagate the failure to the caller (e.g. Main's startup
        // safety net) instead of silently returning an empty list.
        rooms = JsonFileHandler.loadFromFile(FILE_PATH, Room.class);
        if (rooms == null) {
            rooms = new ArrayList<>();
        }
    }

    private void saveRooms() {
        JsonFileHandler.saveToFile(rooms, FILE_PATH);
    }

    public void addRoom(Room room) {
        if (rooms.stream().anyMatch(r -> r.getRoomNumber() == room.getRoomNumber())) {
            throw DuplicateResourceException.forResource("Room", "number", room.getRoomNumber());
        }
        rooms.add(room);
        saveRooms();
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms);
    }

    public Optional<Room> getRoomByNumber(int roomNumber) {
        return rooms.stream().filter(r -> r.getRoomNumber() == roomNumber).findFirst();
    }

    public void updateRoom(Room room) {
        boolean removed = rooms.removeIf(r -> r.getRoomNumber() == room.getRoomNumber());
        if (!removed) {
            throw ResourceNotFoundException.forResource("Room", "number", room.getRoomNumber());
        }
        rooms.add(room);
        saveRooms();
    }

    public void deleteRoom(int roomNumber) {
        boolean removed = rooms.removeIf(r -> r.getRoomNumber() == roomNumber);
        if (!removed) {
            throw ResourceNotFoundException.forResource("Room", "number", roomNumber);
        }
        saveRooms();
    }

    public List<Room> getAvailableRooms() {
        return rooms.stream().filter(Room::isAvailable).toList();
    }
}
