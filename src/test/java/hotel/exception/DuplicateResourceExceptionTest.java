package hotel.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import hotel.model.Room;

class DuplicateResourceExceptionTest {

    @Test
    void factoryProducesStandardMessage() {
        DuplicateResourceException ex = DuplicateResourceException.forResource("Room", "number", 101);
        assertEquals("Room with number 101 already exists", ex.getMessage());
    }

    @Test
    void factoryForCustomer() {
        DuplicateResourceException ex = DuplicateResourceException.forResource("Customer", "ID", "CUST-1");
        assertEquals("Customer with ID CUST-1 already exists", ex.getMessage());
    }

    @Test
    void factoryForReservation() {
        DuplicateResourceException ex = DuplicateResourceException.forResource("Reservation", "ID", "RES-1");
        assertEquals("Reservation with ID RES-1 already exists", ex.getMessage());
    }

    @Test
    void isHotelException() {
        DuplicateResourceException ex = new DuplicateResourceException("test");
        assertTrue(ex instanceof HotelException);
    }

    @Test
    void constructorsPreserveMessageAndCause() {
        Throwable cause = new RuntimeException("duplicate key");
        DuplicateResourceException ex = new DuplicateResourceException("custom", cause);
        assertEquals("custom", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}
