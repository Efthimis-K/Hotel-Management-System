package hotel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotel.exception.ResourceNotFoundException;
import hotel.exception.ValidationException;
import hotel.model.Room;
import hotel.model.RoomType;
import hotel.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    void createRoomAddsValidRoom() {
        Room room = new Room(101, RoomType.SINGLE, 75.0);

        roomService.createRoom(room);

        verify(roomRepository).addRoom(room);
    }

    @Test
    void createRoomRejectsNullRoom() {
        assertThrows(ValidationException.class, () -> roomService.createRoom(null));
    }

    @Test
    void createRoomRejectsZeroRoomNumber() {
        Room room = new Room();
        room.setRoomType(RoomType.SINGLE);
        room.setPricePerNight(50.0);
        assertThrows(ValidationException.class, () -> roomService.createRoom(room));
    }

    @Test
    void createRoomRejectsZeroPrice() {
        Room room = new Room();
        room.setRoomNumber(201);
        room.setRoomType(RoomType.SINGLE);
        assertThrows(ValidationException.class, () -> roomService.createRoom(room));
    }

    @Test
    void createRoomRejectsDefaultRoomWithZeroPrice() {
        Room room = new Room();
        room.setRoomNumber(202);
        room.setRoomType(RoomType.SINGLE);
        assertThrows(ValidationException.class, () -> roomService.createRoom(room));
    }

    @Test
    void updateRoomAvailabilityPersistsUpdatedFlag() {
        Room room = new Room(103, RoomType.SUITE, 140.0);
        when(roomRepository.getRoomByNumber(103)).thenReturn(Optional.of(room));

        roomService.updateRoomAvailability(103, false);

        assertFalse(room.isAvailable());
        verify(roomRepository).updateRoom(room);
    }

    @Test
    void updateRoomAvailabilityRejectsMissingRoom() {
        when(roomRepository.getRoomByNumber(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> roomService.updateRoomAvailability(999, true)
        );

        assertEquals("Room with number 999 not found", exception.getMessage());
    }

    @Test
    void getAllRoomsReturnsRepositoryList() {
        Room room = new Room(301, RoomType.SINGLE, 80.0);
        when(roomRepository.getAllRooms()).thenReturn(List.of(room));

        var rooms = roomService.getAllRooms();

        assertEquals(1, rooms.size());
        assertEquals(301, rooms.getFirst().getRoomNumber());
    }

    @Test
    void getAvailableRoomsReturnsRepositoryList() {
        Room room = new Room(302, RoomType.DOUBLE, 120.0);
        when(roomRepository.getAvailableRooms()).thenReturn(List.of(room));

        var rooms = roomService.getAvailableRooms();

        assertEquals(1, rooms.size());
        assertEquals(302, rooms.getFirst().getRoomNumber());
    }

    @Test
    void getRoomByNumberReturnsRepositoryResult() {
        Room room = new Room(303, RoomType.SUITE, 200.0);
        when(roomRepository.getRoomByNumber(303)).thenReturn(Optional.of(room));

        var result = roomService.getRoomByNumber(303);

        assertTrue(result.isPresent());
        assertEquals(303, result.get().getRoomNumber());
    }
}
