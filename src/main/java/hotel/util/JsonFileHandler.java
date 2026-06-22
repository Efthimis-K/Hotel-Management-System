package hotel.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import hotel.exception.StorageException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class JsonFileHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    /** Timeout for acquiring a file lock (milliseconds). */
    private static final long LOCK_TIMEOUT_MS = 5000;
    /** Interval between lock acquisition attempts (milliseconds). */
    private static final long LOCK_POLL_INTERVAL_MS = 50;

    /**
     * Holder for a file lock and its associated RandomAccessFile.
     * Ensures both are released together.
     */
    private static class LockHandle {
        final FileLock lock;
        final RandomAccessFile raf;

        /**
         * Creates a lock handle that pairs a file lock with its underlying resource for synchronized release.
         *
         * @param lock the file lock to manage
         * @param raf  the random access file associated with the lock
         */
        LockHandle(FileLock lock, RandomAccessFile raf) {
            this.lock = lock;
            this.raf = raf;
        }
    }

    private JsonFileHandler() {
        // utility class
    }

    /**
     * Saves a list to file as JSON with exclusive file locking.
     *
     * Creates parent directories as needed. The write operation is protected
     * by an exclusive file lock to coordinate concurrent access.
     *
     * @throws StorageException if the parent directory cannot be created, JSON
     *         serialization fails, or I/O operations fail
     */
    public static <T> void saveToFile(List<T> data, String filePath) {
        File file = resolveFile(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs() && !parentDir.exists()) {
                throw StorageException.forFile(filePath,
                    new IOException("Failed to create parent directory: " + parentDir));
            }
        }

        File lockFile = getLockFile(file);
        LockHandle handle = null;
        try {
            handle = acquireLock(lockFile);
            try {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
            } catch (JsonProcessingException e) {
                throw StorageException.forFile(filePath, e);
            } catch (IOException e) {
                throw StorageException.forFile(filePath, e);
            }
        } finally {
            releaseLock(handle);
        }
    }

    /**
     * Loads and deserializes a JSON list from the specified file.
     *
     * @param filePath the path to the JSON file
     * @param clazz the class type for elements in the returned list
     * @return a list of deserialized objects; an empty list if the file does not exist
     * @throws StorageException if file resolution fails, lock acquisition times out, or deserialization fails
     */
    public static <T> List<T> loadFromFile(String filePath, Class<T> clazz) {
        File file;
        try {
            file = resolveFile(filePath);
        } catch (RuntimeException e) {
            throw StorageException.forFile(filePath, e);
        }

        File lockFile = getLockFile(file);
        LockHandle handle = null;
        try {
            handle = acquireLock(lockFile);
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
        } finally {
            releaseLock(handle);
        }
    }

    /**
     * Acquires an exclusive file lock, retrying until successful or timeout.
     *
     * @return a LockHandle containing the acquired FileLock and RandomAccessFile
     * @throws StorageException if lock acquisition times out or fails due to I/O error
     */
    private static LockHandle acquireLock(File lockFile) {
        RandomAccessFile raf = null;
        FileLock lock = null;
        long startTime = System.currentTimeMillis();

        while (true) {
            try {
                raf = new RandomAccessFile(lockFile, "rw");
                lock = raf.getChannel().tryLock();
                if (lock != null) {
                    return new LockHandle(lock, raf);
                }
                // Lock not available, close and retry
                raf.close();
            } catch (OverlappingFileLockException e) {
                // Already locked by this JVM - close and retry
                if (raf != null) {
                    try { raf.close(); } catch (IOException ignored) {}
                }
            } catch (IOException e) {
                if (raf != null) {
                    try { raf.close(); } catch (IOException ignored) {}
                }
                throw StorageException.forFile(lockFile.getPath(),
                    new IOException("Failed to open lock file: " + lockFile, e));
            }

            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= LOCK_TIMEOUT_MS) {
                throw StorageException.forFile(lockFile.getPath(),
                    new IOException("Timeout acquiring lock on " + lockFile + " after " + LOCK_TIMEOUT_MS + "ms"));
            }

            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(LOCK_POLL_INTERVAL_MS));
        }
    }

    /**
     * Releases the file lock and closes the associated RandomAccessFile.
     * Handles any errors gracefully without throwing exceptions.
     */
    private static void releaseLock(LockHandle handle) {
        if (handle == null) {
            return;
        }
        if (handle.lock != null) {
            try {
                handle.lock.release();
            } catch (IOException e) {
                // Log but don't throw - best effort release
                System.err.println("Warning: Failed to release file lock: " + e.getMessage());
            }
        }
        if (handle.raf != null) {
            try {
                handle.raf.close();
            } catch (IOException e) {
                System.err.println("Warning: Failed to close lock file: " + e.getMessage());
            }
        }
    }

    /**
     * Constructs the lock file path for a given data file.
     *
     * @return the lock file with .lock extension in the same directory as the data file
     */
    private static File getLockFile(File dataFile) {
        return new File(dataFile.getParentFile(), dataFile.getName() + ".lock");
    }

    /**
     * Resolves a file path, using the Maven project root as the base directory for relative paths.
     *
     * For absolute paths, returns the normalized path. For relative paths, walks the directory
     * tree upward from the current working directory searching for a pom.xml file. If found,
     * the path is resolved relative to that directory. Otherwise, the path is resolved relative
     * to the current working directory.
     *
     * @param filePath the file path to resolve
     * @return the resolved, normalized File
     */
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
