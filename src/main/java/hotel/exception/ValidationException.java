package hotel.exception;

/**
 * Thrown when domain validation fails: bad input format, out-of-range
 * numbers, null/blank required fields, invalid date ranges, etc.
 * <p>
 * Replaces IllegalArgumentException in model setters and service-level
 * business validation. The UI prefixes its message with "Invalid input: ".
 */
public class ValidationException extends HotelException {

    private static final long serialVersionUID = 1L;

    public ValidationException() {
        super();
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Builds a field-specific validation message like "Field 'pricePerNight':
     * must be positive".
     */
    public static ValidationException forField(String fieldName, String reason) {
        return new ValidationException("Field '" + fieldName + "': " + reason);
    }

    @Override
    public String getUserMessage() {
        return "Invalid input: " + getMessage();
    }
}
