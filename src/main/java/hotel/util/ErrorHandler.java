package hotel.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import hotel.exception.DuplicateResourceException;
import hotel.exception.HotelException;
import hotel.exception.ResourceNotFoundException;
import hotel.exception.StorageException;
import hotel.exception.ValidationException;

/**
 * Central UI exception handler. Maps exceptions thrown by the service and
 * repository layers to user-friendly messages and logs them at the
 * appropriate severity via {@link java.util.logging.Logger}.
 * <p>
 * Dispatch order is important: more specific subclasses are checked first so
 * e.g. a {@link StorageException} is reported as a storage error (not just a
 * generic HotelException).
 */
public final class ErrorHandler {

    private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class.getName());

    private ErrorHandler() {
        // utility class
    }

    /**
     * Handles a runtime exception thrown by a service or repository call.
     * Logs the throwable and returns a message safe to show to the user.
     * Never re-throws.
     *
     * @param t      the throwable
     * @param logger a per-component logger (may be null, in which case the
     *               default {@link #LOGGER} is used)
     * @return a user-friendly message
     */
    public static String handle(Throwable t, Logger logger) {
        Logger effectiveLogger = logger != null ? logger : LOGGER;
        if (t == null) {
            return "An unknown error occurred.";
        }
        if (t instanceof StorageException) {
            effectiveLogger.log(Level.WARNING, "Storage error", t);
            return ((HotelException) t).getUserMessage();
        }
        if (t instanceof ValidationException) {
            effectiveLogger.log(Level.FINE, "Validation error: {0}", t.getMessage());
            return ((HotelException) t).getUserMessage();
        }
        if (t instanceof DuplicateResourceException) {
            effectiveLogger.log(Level.FINE, "Duplicate resource: {0}", t.getMessage());
            return t.getMessage();
        }
        if (t instanceof ResourceNotFoundException) {
            effectiveLogger.log(Level.FINE, "Resource not found: {0}", t.getMessage());
            return t.getMessage();
        }
        if (t instanceof HotelException) {
            effectiveLogger.log(Level.WARNING, "Hotel error", t);
            return t.getMessage();
        }
        // Unexpected — log with full stack trace and return a generic message.
        effectiveLogger.log(Level.SEVERE, "Unexpected error", t);
        return "An unexpected error occurred. Please try again.";
    }

    /**
     * Handles a fatal error that occurred during application startup
     * (e.g. a corrupt data file that prevents the repositories from
     * loading). Logs at SEVERE and returns a single, concise message.
     */
    public static String handleStartupError(Throwable t, Logger logger) {
        Logger effectiveLogger = logger != null ? logger : LOGGER;
        if (t == null) {
            return "Unknown startup error.";
        }
        effectiveLogger.log(Level.SEVERE, "Startup error", t);
        if (t instanceof StorageException) {
            return "Failed to start: " + ((HotelException) t).getUserMessage();
        }
        return "Failed to start: " + (t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName());
    }
}
