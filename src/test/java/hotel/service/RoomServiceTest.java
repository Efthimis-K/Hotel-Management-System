package hotel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void createRoomRejectsNonPositiveRoomNumber() {
        Room room = new Room();
        assertThrows(ValidationException.class, () -> room.setRoomNumber(-1));
    }

    @Test
    void createRoomRejectsNonPositivePrice() {
        Room room = new Room();
        room.setRoomNumber(102);
        room.setRoomType(RoomType.DOUBLE);
        assertThrows(ValidationException.class, () -> room.setPricePerNight(-10.0));
    }

    @Test
    void createRoomRejectsNullRoom() {
        assertThrows(ValidationException.class, () -> roomService.createRoom(null));
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
}
