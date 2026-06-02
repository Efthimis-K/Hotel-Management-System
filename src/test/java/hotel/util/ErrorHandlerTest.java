package hotel.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import hotel.exception.DuplicateResourceException;
import hotel.exception.HotelException;
import hotel.exception.ResourceNotFoundException;
import hotel.exception.StorageException;
import hotel.exception.ValidationException;

class ErrorHandlerTest {

    private final Logger logger = Logger.getLogger("test.logger");

    @Test
    void handleReturnsUserMessageForValidationException() {
        String msg = ErrorHandler.handle(new ValidationException("bad email"), logger);
        assertEquals("Invalid input: bad email", msg);
    }

    @Test
    void handleReturnsUserMessageForDuplicateResourceException() {
        DuplicateResourceException ex = DuplicateResourceException.forResource("Room", "number", 101);
        String msg = ErrorHandler.handle(ex, logger);
        assertEquals("Room with number 101 already exists", msg);
    }

    @Test
    void handleReturnsUserMessageForResourceNotFoundException() {
        ResourceNotFoundException ex = ResourceNotFoundException.forResource("Reservation", "ID", "RES-X");
        String msg = ErrorHandler.handle(ex, logger);
        assertEquals("Reservation with ID RES-X not found", msg);
    }

    @Test
    void handleReturnsStorageMessageForStorageException() {
        StorageException ex = StorageException.forFile("data/x.json", new RuntimeException("disk full"));
        String msg = ErrorHandler.handle(ex, logger);
        assertTrue(msg.startsWith("A storage error occurred:"));
        assertTrue(msg.contains("disk full"));
        assertNotNull(ex.getCause());
    }

    @Test
    void handleReturnsGenericMessageForUnexpectedException() {
        String msg = ErrorHandler.handle(new RuntimeException("boom"), logger);
        assertEquals("An unexpected error occurred. Please try again.", msg);
    }

    @Test
    void handleReturnsGenericMessageForNullThrowable() {
        String msg = ErrorHandler.handle(null, logger);
        assertEquals("An unknown error occurred.", msg);
    }

    @Test
    void handleReturnsMessageForOtherHotelException() {
        String msg = ErrorHandler.handle(new HotelException("custom problem"), logger);
        assertEquals("custom problem", msg);
    }

    @Test
    void handleStartupErrorIncludesMessage() {
        String msg = ErrorHandler.handleStartupError(new RuntimeException("init failed"), logger);
        assertTrue(msg.contains("init failed"));
    }

    @Test
    void handleAcceptsNullLoggerWithoutNpe() {
        String msg = ErrorHandler.handle(new ValidationException("x"), null);
        assertEquals("Invalid input: x", msg);
    }
}
