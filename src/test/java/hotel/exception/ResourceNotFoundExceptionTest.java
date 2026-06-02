package hotel.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

    @Test
    void resourceNotFoundFactoryProducesStandardMessage() {
        ResourceNotFoundException ex = ResourceNotFoundException.forResource("Room", "number", 42);
        assertEquals("Room with number 42 not found", ex.getMessage());
        assertTrue(ex instanceof HotelException);
    }

    @Test
    void constructorsPreserveMessageAndCause() {
        Throwable cause = new RuntimeException("disk");
        ResourceNotFoundException ex = new ResourceNotFoundException("custom", cause);
        assertEquals("custom", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}
