package service;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class Managers {
    public static HttpTaskManager getDefault(URI uri) throws IOException, InterruptedException {
        return new HttpTaskManager(uri);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getFileBackedTaskManager() {
        try {
            return FileBackedTasksManager.loadFromFile(new File("resources" + File.separator + "dbForTests.csv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TaskManager getInMemoryTaskManager() {
        return new InMemoryTaskManager();
    }
}