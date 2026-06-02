package hotel.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import hotel.exception.StorageException;
import hotel.model.Room;
import hotel.model.RoomType;

class JsonFileHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void saveToFileCreatesParentDirectoriesAndPersistsData() throws IOException {
        Path filePath = tempDir.resolve("nested").resolve("rooms.json");
        List<Room> rooms = List.of(
            new Room(101, RoomType.SINGLE, 50.0),
            new Room(102, RoomType.DELUXE, 200.0)
        );

        JsonFileHandler.saveToFile(rooms, filePath.toString());
        List<Room> loadedRooms = JsonFileHandler.loadFromFile(filePath.toString(), Room.class);

        assertTrue(Files.exists(filePath));
        assertEquals(2, loadedRooms.size());
        assertEquals(101, loadedRooms.get(0).getRoomNumber());
        assertEquals(RoomType.DELUXE, loadedRooms.get(1).getRoomType());
    }

    @Test
    void loadFromFileReturnsEmptyListWhenFileDoesNotExist() throws IOException {
        Path missingFile = tempDir.resolve("missing.json");

        List<Room> loadedRooms = JsonFileHandler.loadFromFile(missingFile.toString(), Room.class);

        assertTrue(loadedRooms.isEmpty());
    }

    @Test
    void saveToFileResolvesRelativePathFromProjectRootWhenRunningInSubdirectory() throws IOException {
        Path projectRoot = tempDir.resolve("project-root");
        Path executionDir = projectRoot.resolve("src").resolve("main");
        Path expectedFile = projectRoot.resolve("tmp-test-data").resolve("rooms.json");
        Path unexpectedFile = executionDir.resolve("tmp-test-data").resolve("rooms.json");
        Files.createDirectories(executionDir);
        Files.writeString(projectRoot.resolve("pom.xml"), "<project/>");

        List<Room> rooms = List.of(new Room(301, RoomType.SUITE, 150.0));
        String originalUserDir = System.getProperty("user.dir");

        try {
            System.setProperty("user.dir", executionDir.toString());
            JsonFileHandler.saveToFile(rooms, "tmp-test-data/rooms.json");
        } finally {
            System.setProperty("user.dir", originalUserDir);
        }

        assertTrue(Files.exists(expectedFile));
        assertTrue(Files.notExists(unexpectedFile));
    }

    @Test
    void loadFromFileThrowsStorageExceptionForMalformedJson() throws IOException {
        Path malformedFile = tempDir.resolve("malformed.json");
        Files.writeString(malformedFile, "{ this is not : valid json ]");

        StorageException ex = assertThrows(
            StorageException.class,
            () -> JsonFileHandler.loadFromFile(malformedFile.toString(), Room.class)
        );

        assertNotNull(ex.getCause());
        assertTrue(ex.getMessage().contains("Failed to access"));
    }
}
