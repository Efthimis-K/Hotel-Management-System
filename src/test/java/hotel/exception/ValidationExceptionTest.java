package hotel.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValidationExceptionTest {

    @Test
    void validationExceptionIsHotelException() {
        ValidationException ex = new ValidationException("x");
        assertTrue(ex instanceof HotelException);
    }

    @Test
    void factoryProducesFieldSpecificMessage() {
        ValidationException ex = ValidationException.forField("pricePerNight", "must be positive");
        assertEquals("Field 'pricePerNight': must be positive", ex.getMessage());
    }

    @Test
    void userMessagePrefixesInvalidInput() {
        ValidationException ex = new ValidationException("bad email");
        assertEquals("Invalid input: bad email", ex.getUserMessage());
    }

    @Test
    void constructorsAndCausePreserved() {
        Throwable cause = new IllegalStateException("original");
        ValidationException ex = new ValidationException("wrap", cause);
        assertEquals("wrap", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void noArgConstructorWorks() {
        ValidationException ex = new ValidationException();
        assertNotNull(ex);
    }
}
