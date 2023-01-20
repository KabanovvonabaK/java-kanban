package service;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest extends HistoryManagerTest<InMemoryHistoryManager> {

    @Override
    InMemoryHistoryManager createHistoryManager() {
        return new InMemoryHistoryManager();
    }
}