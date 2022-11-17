package service;

import model.Task;

import java.util.LinkedList;

public class InMemoryHistoryManager implements HistoryManager {
    private final LinkedList<Task> history;
    private static final int HISTORY_SIZE = 10;

    public InMemoryHistoryManager() {
        history = new LinkedList<>();
    }

    @Override
    public void add(Task task) {
        if (history.size() >= HISTORY_SIZE) {
            history.remove(0);
        }
        history.add(task);
    }

    @Override
    public LinkedList<Task> getHistory() {
        return history;
    }
}