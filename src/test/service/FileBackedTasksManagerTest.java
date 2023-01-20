package service;

import java.io.File;

class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {

    @Override
    FileBackedTasksManager createTaskManager() {
        return new FileBackedTasksManager(new File("resources" + File.separator + "dbForTests.csv"));
    }
}