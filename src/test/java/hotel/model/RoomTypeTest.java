package hotel.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RoomTypeTest {

    @Test
    void containsAllFourRoomTypes() {
        RoomType[] types = RoomType.values();
        assertEquals(4, types.length);
    }

    @Test
    void singleHasCorrectPrice() {
        assertEquals(50.0, RoomType.SINGLE.getDefaultPrice());
    }

    @Test
    void doubleHasCorrectPrice() {
        assertEquals(80.0, RoomType.DOUBLE.getDefaultPrice());
    }

    @Test
    void suiteHasCorrectPrice() {
        assertEquals(150.0, RoomType.SUITE.getDefaultPrice());
    }

    @Test
    void deluxeHasCorrectPrice() {
        assertEquals(200.0, RoomType.DELUXE.getDefaultPrice());
    }

    @Test
    void singleHasCorrectDescription() {
        assertEquals("Single Room", RoomType.SINGLE.getDescription());
    }

    @Test
    void doubleHasCorrectDescription() {
        assertEquals("Double Room", RoomType.DOUBLE.getDescription());
    }

    @Test
    void suiteHasCorrectDescription() {
        assertEquals("Suite", RoomType.SUITE.getDescription());
    }

    @Test
    void deluxeHasCorrectDescription() {
        assertEquals("Deluxe Suite", RoomType.DELUXE.getDescription());
    }

    @Test
    void valueOfReturnsCorrectEnum() {
        assertEquals(RoomType.SINGLE, RoomType.valueOf("SINGLE"));
        assertEquals(RoomType.DOUBLE, RoomType.valueOf("DOUBLE"));
        assertEquals(RoomType.SUITE, RoomType.valueOf("SUITE"));
        assertEquals(RoomType.DELUXE, RoomType.valueOf("DELUXE"));
    }
}
