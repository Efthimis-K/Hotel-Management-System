package hotel.exception;

/**
 * Thrown when a referenced entity (room, customer, reservation) does not exist.
 * Replaces ad-hoc IllegalArgumentException("... not found") in repositories
 * and services.
 */
public class ResourceNotFoundException extends HotelException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Builds a standard "X with field Y not found" message.
     */
    public static ResourceNotFoundException forResource(String resourceType, String field, Object value) {
        return new ResourceNotFoundException(
            resourceType + " with " + field + " " + value + " not found");
    }
}
