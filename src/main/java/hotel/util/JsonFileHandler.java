package hotel.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import hotel.exception.StorageException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonFileHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    private JsonFileHandler() {
        // utility class
    }

    public static <T> void saveToFile(List<T> data, String filePath) {
        File file = resolveFile(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs() && !parentDir.exists()) {
                throw StorageException.forFile(filePath,
                    new IOException("Failed to create parent directory: " + parentDir));
            }
        }
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        } catch (JsonProcessingException e) {
            throw StorageException.forFile(filePath, e);
        } catch (IOException e) {
            throw StorageException.forFile(filePath, e);
        }
    }

    public static <T> List<T> loadFromFile(String filePath, Class<T> clazz) {
        File file;
        try {
            file = resolveFile(filePath);
        } catch (RuntimeException e) {
            throw StorageException.forFile(filePath, e);
        }
        if (!file.exists()) {
            return new ArrayList<>();
        }
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
        try {
            return objectMapper.readValue(file, type);
        } catch (JsonProcessingException e) {
            throw StorageException.forFile(filePath, e);
        } catch (IOException e) {
            throw StorageException.forFile(filePath, e);
        }
    }

    private static File resolveFile(String filePath) {
        Path path = Path.of(filePath);
        if (path.isAbsolute()) {
            return path.normalize().toFile();
        }

        String userDir = System.getProperty("user.dir", ".");
        Path current = Path.of(userDir).toAbsolutePath().normalize();
        while (current != null) {
            if (Files.exists(current.resolve("pom.xml"))) {
                return current.resolve(path).normalize().toFile();
            }
            current = current.getParent();
        }

        return path.toAbsolutePath().normalize().toFile();
    }
}
