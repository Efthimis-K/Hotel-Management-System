package hotel.exception;

/**
 * Base unchecked exception for all hotel-domain errors.
 * <p>
 * The console UI catches this type and dispatches on its subtypes
 * (ResourceNotFoundException, DuplicateResourceException,
 * ValidationException, StorageException) to produce user-friendly messages.
 */
public class HotelException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public HotelException() {
        super();
    }

    public HotelException(String message) {
        super(message);
    }

    public HotelException(String message, Throwable cause) {
        super(message, cause);
    }

    public HotelException(Throwable cause) {
        super(cause);
    }

    /**
     * Wraps an existing throwable as a HotelException with a contextual message,
     * preserving the original cause for the stack trace.
     */
    public static HotelException wrap(Throwable cause, String message) {
        if (cause instanceof HotelException he) {
            return he;
        }
        return new HotelException(message, cause);
    }

    /**
     * Returns a message safe to show to end users. By default this is the
     * exception's own message. Subclasses may override to add prefixes
     * (e.g. "Invalid input: ...").
     */
    public String getUserMessage() {
        return getMessage();
    }
}
