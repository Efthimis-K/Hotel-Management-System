package hotel.exception;

/**
 * Thrown when an add/create operation is attempted with an identifier
 * that already exists in storage.
 */
public class DuplicateResourceException extends HotelException {

    private static final long serialVersionUID = 1L;

    public DuplicateResourceException() {
        super();
    }

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Builds a standard "X with field Y already exists" message.
     */
    public static DuplicateResourceException forResource(String resourceType, String field, Object value) {
        return new DuplicateResourceException(
            resourceType + " with " + field + " " + value + " already exists");
    }
}
