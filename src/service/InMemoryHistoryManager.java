package service;

import model.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private final CustomLinkedList<Task> history;

    public InMemoryHistoryManager() {
        history = new CustomLinkedList<>();
    }

    @Override
    public void add(Task task) {
        history.linkLast(task);
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history.getTasks();
    }

    @Override
    public void remove(int id) {
        ArrayList<Task> tasks = history.getTasks();
        history.removeNode(new Node(tasks.get(id)));
    }
}