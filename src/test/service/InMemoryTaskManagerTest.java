package service;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager>{

    @Override
    InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }
}