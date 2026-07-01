package hotel.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BackgroundTaskService {

    private static final ExecutorService INSTANCE = Executors.newCachedThreadPool();

    private BackgroundTaskService() {
    }

    public static ExecutorService getExecutor() {
        return INSTANCE;
    }

    public static void shutdown() {
        INSTANCE.shutdown();
    }
}