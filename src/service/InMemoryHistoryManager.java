package service;

import model.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private final ArrayList<Task> history;
    private static final int HISTORY_SIZE = 10;

    public InMemoryHistoryManager() {
        history = new ArrayList<>();
    }

    @Override
    public void add(Task task) {
        if (history.size() < HISTORY_SIZE) {
            history.add(task);
        } else {
            history.remove(0);
            history.add(task);
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history;
    }
}