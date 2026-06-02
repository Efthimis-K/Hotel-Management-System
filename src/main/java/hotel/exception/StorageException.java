package hotel.exception;

/**
 * Thrown when persistence (file IO, JSON serialization/deserialization) fails.
 * Always preserves the underlying IOException or JsonProcessingException
 * as the cause.
 */
public class StorageException extends HotelException {

    private static final long serialVersionUID = 1L;

    public StorageException() {
        super();
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Builds a file-context storage exception, preserving the original IO/JSON
     * error as the cause.
     */
    public static StorageException forFile(String filePath, Throwable cause) {
        String causeMessage = cause != null && cause.getMessage() != null
            ? cause.getMessage()
            : (cause != null ? cause.getClass().getSimpleName() : "unknown error");
        return new StorageException("Failed to access '" + filePath + "': " + causeMessage, cause);
    }

    @Override
    public String getUserMessage() {
        Throwable cause = getCause();
        String causeMessage = cause != null && cause.getMessage() != null
            ? cause.getMessage()
            : (cause != null ? cause.getClass().getSimpleName() : "unknown error");
        return "A storage error occurred: " + causeMessage;
    }
}
