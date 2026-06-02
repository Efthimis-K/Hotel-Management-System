package hotel.service;

import hotel.exception.ResourceNotFoundException;
import hotel.exception.ValidationException;
import hotel.model.Room;
import hotel.repository.RoomRepository;

import java.util.List;
import java.util.Optional;

public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public void createRoom(Room room) {
        if (room == null) {
            throw ValidationException.forField("room", "must not be null");
        }
        if (room.getRoomNumber() <= 0) {
            throw new ValidationException("Room number must be positive");
        }
        if (room.getPricePerNight() <= 0) {
            throw new ValidationException("Price per night must be positive");
        }
        roomRepository.addRoom(room);
    }

    public List<Room> getAllRooms() {
        return roomRepository.getAllRooms();
    }

    public List<Room> getAvailableRooms() {
        return roomRepository.getAvailableRooms();
    }

    public Optional<Room> getRoomByNumber(int roomNumber) {
        return roomRepository.getRoomByNumber(roomNumber);
    }

    public void updateRoomAvailability(int roomNumber, boolean available) {
        Optional<Room> roomOpt = roomRepository.getRoomByNumber(roomNumber);
        if (roomOpt.isEmpty()) {
            throw ResourceNotFoundException.forResource("Room", "number", roomNumber);
        }
        Room room = roomOpt.get();
        room.setAvailable(available);
        roomRepository.updateRoom(room);
    }
}
