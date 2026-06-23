package hotel.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import hotel.exception.ValidationException;

class RoomTest {

    @Test
    void constructorSetsDefaultAvailabilityTrue() {
        Room room = new Room(101, RoomType.SINGLE, 75.0);
        assertTrue(room.isAvailable());
    }

    @Test
    void noArgConstructorAlsoSetsDefaultAvailabilityTrue() {
        Room room = new Room();
        assertTrue(room.isAvailable());
    }

    @Test
    void setRoomTypeRejectsNull() {
        Room room = new Room();
        assertThrows(ValidationException.class, () -> room.setRoomType(null));
    }

    @Test
    void setPricePerNightRejectsNaN() {
        Room room = new Room();
        assertThrows(ValidationException.class, () -> room.setPricePerNight(Double.NaN));
    }

    @Test
    void setPricePerNightRejectsInfinite() {
        Room room = new Room();
        assertThrows(ValidationException.class, () -> room.setPricePerNight(Double.POSITIVE_INFINITY));
    }

    @Test
    void setPricePerNightRejectsNonPositive() {
        Room room = new Room();
        assertThrows(ValidationException.class, () -> room.setPricePerNight(0.0));
        assertThrows(ValidationException.class, () -> room.setPricePerNight(-10.0));
    }

    @Test
    void setPricePerNightAcceptsPositive() {
        Room room = new Room();
        room.setPricePerNight(99.99);
        assertEquals(99.99, room.getPricePerNight());
    }

    @Test
    void toggleAvailabilityFlipsFlag() {
        Room room = new Room(201, RoomType.DOUBLE, 120.0);
        assertTrue(room.isAvailable());
        room.toggleAvailability();
        assertFalse(room.isAvailable());
        room.toggleAvailability();
        assertTrue(room.isAvailable());
    }

    @Test
    void setAvailableUpdatesFlag() {
        Room room = new Room(202, RoomType.SUITE, 200.0);
        room.setAvailable(false);
        assertFalse(room.isAvailable());
    }

    @Test
    void equalsAndHashCodeByRoomNumber() {
        Room room1 = new Room(301, RoomType.SINGLE, 50.0);
        Room room2 = new Room(301, RoomType.DOUBLE, 80.0);
        Room room3 = new Room(302, RoomType.SINGLE, 50.0);

        assertEquals(room1, room2);
        assertEquals(room1.hashCode(), room2.hashCode());
        assertFalse(room1.equals(room3));
        assertNotEquals(null, room1);
        assertTrue(room1.equals(room1));
    }

    @Test
    void setRoomNumberRejectsNonPositive() {
        Room room = new Room();
        assertThrows(ValidationException.class, () -> room.setRoomNumber(0));
        assertThrows(ValidationException.class, () -> room.setRoomNumber(-5));
    }

    @Test
    void setRoomNumberAcceptsPositive() {
        Room room = new Room();
        room.setRoomNumber(404);
        assertEquals(404, room.getRoomNumber());
    }

    @Test
    void constructorRejectsInvalidRoomNumber() {
        assertThrows(ValidationException.class, () -> new Room(-1, RoomType.SINGLE, 50.0));
    }

    @Test
    void constructorRejectsInvalidPrice() {
        assertThrows(ValidationException.class, () -> new Room(101, RoomType.SINGLE, 0.0));
        assertThrows(ValidationException.class, () -> new Room(101, RoomType.SINGLE, -10.0));
    }

    @Test
    void toStringContainsRoomDetails() {
        Room room = new Room(505, RoomType.DELUXE, 250.0);
        String str = room.toString();
        assertTrue(str.contains("505"));
        assertTrue(str.contains("DELUXE"));
        assertTrue(str.contains("250.0"));
    }
}
