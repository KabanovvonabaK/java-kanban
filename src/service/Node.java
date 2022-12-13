package service;

import model.Task;

public class Node {

    Task task;

    public Node(Task task) {
        this.task = task;
    }

    @Override
    public boolean equals(Object obj) {
        Node node = (Node) obj;
        Task taskToCompare = node.task;
        return task.equals(taskToCompare);
    }

    @Override
    public int hashCode() {
        return task.hashCode();
    }
}